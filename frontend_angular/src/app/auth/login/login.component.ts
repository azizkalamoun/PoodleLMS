import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../core/services/auth.service';
import { SnackBarService } from '../../core/services/snackbar.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './login.component.html',
  styles: [
    `
      :host {
        display: block;
      }

      .bg-cover {
        background-size: cover;
      }

      .bg-center {
        background-position: center;
      }

      .bg-no-repeat {
        background-repeat: no-repeat;
      }
    `,
  ],
})
export class LoginComponent {
  loginForm: FormGroup;
  hidePassword = true;
  loading = false;
  backgroundImageUrl: string | null = null;
  logoImageUrl: string | null = null;
  useDefaultLogo = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private notify: SnackBarService,
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]],
    });

    this.initializeImages();

    // If already logged in, redirect based on role
    // Note: Using setTimeout to avoid redirect during component initialization
    if (this.authService.isLoggedIn()) {
      setTimeout(() => {
        const user = this.authService.getCurrentUser();
        if (user?.role === 'ROLE_ADMIN') {
          this.router.navigate(['/admin/dashboard']);
        } else if (user?.role === 'ROLE_EMPLOYEE') {
          this.router.navigate(['/employee/dashboard']);
        }
      }, 0);
    }
  }

  private initializeImages(): void {
    // Try to load background image - prefer PNG for better quality, fallback to SVG
    const pngUrl = '/assets/images/login-background.png';
    const svgUrl = '/assets/images/login-background.svg';

    const img = new Image();
    img.onload = () => {
      this.backgroundImageUrl = pngUrl;
    };
    img.onerror = () => {
      // If PNG fails, use SVG fallback
      this.backgroundImageUrl = svgUrl;
    };
    img.src = pngUrl;

    // Try to load logo image with fallback
    const logoUrl = '/assets/images/logo.png';
    const logoImg = new Image();
    logoImg.onload = () => {
      this.logoImageUrl = logoUrl;
    };
    logoImg.onerror = () => {
      // If custom logo fails to load, use default icon
      this.useDefaultLogo = true;
    };
    logoImg.src = logoUrl;
  }

  onSubmit(): void {
    if (this.loginForm.invalid) return;

    const email = this.loginForm.value.email;
    console.log(' [LOGIN-COMPONENT] Submitting login form for:', email);

    this.loading = true;
    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        console.log(' [LOGIN-COMPONENT] Login response received');
        const user = this.authService.getCurrentUser();
        console.log(' [LOGIN-COMPONENT] Current user after login:', user);

        this.notify.success('Login successful!');

        console.log('⏱  [LOGIN-COMPONENT] Waiting 500ms before redirect...');
        // Navigate after a brief delay to ensure state is updated
        setTimeout(() => {
          console.log(' [LOGIN-COMPONENT] Executing redirectByRole()');
          this.redirectByRole();
        }, 500);
      },
      error: (err) => {
        this.loading = false;
        console.error(' [LOGIN-COMPONENT] Login error:', err);
        this.notify.error(
          err.error?.message || 'Login failed. Please check your credentials.',
        );
      },
    });
  }

  private redirectByRole(): void {
    const user = this.authService.getCurrentUser();
    console.log('[LOGIN-COMPONENT] Checking user role:', user?.role);
    console.log('[LOGIN-COMPONENT] Full user object:', user);

    if (user?.role === 'ROLE_ADMIN') {
      console.log('[LOGIN-COMPONENT] Redirecting to admin dashboard');
      this.router.navigate(['/admin/dashboard']);
    } else if (user?.role === 'ROLE_EMPLOYEE') {
      console.log('[LOGIN-COMPONENT] Redirecting to employee dashboard');
      this.router.navigate(['/employee/dashboard']);
    } else {
      console.warn(
        '[LOGIN-COMPONENT] Unknown role, redirecting to employee dashboard:',
        user?.role,
      );
      this.router.navigate(['/employee/dashboard']);
    }
  }
}
