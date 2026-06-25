import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../../core/services/auth.service';
import { LogoService, LogoConfig } from '../../core/services/logo.service';

interface NavItem {
  label: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    MatTooltipModule,
  ],
  templateUrl: './admin-layout.component.html',
})
export class AdminLayoutComponent implements OnInit {
  collapsed = signal(false);
  currentUser = this.authService.getCurrentUser();
  logoConfig: LogoConfig = { logoImageUrl: null, useDefaultLogo: false };

  navItems: NavItem[] = [
    { label: 'Dashboard', icon: 'dashboard', route: '/admin/dashboard' },
    { label: 'Employees', icon: 'people', route: '/admin/employees' },
    { label: 'Departments', icon: 'account_tree', route: '/admin/departments' },
    { label: 'Courses', icon: 'menu_book', route: '/admin/courses' },
    {
      label: 'Certificates',
      icon: 'verified_user',
      route: '/admin/certificates',
    },
    { label: 'Analytics', icon: 'bar_chart', route: '/admin/analytics' },
  ];

  constructor(
    private authService: AuthService,
    private logoService: LogoService,
  ) {}

  ngOnInit(): void {
    // Subscribe to logo configuration updates
    this.logoService.logoConfig$.subscribe((config) => {
      this.logoConfig = config;
    });
  }

  toggleSidebar(): void {
    this.collapsed.set(!this.collapsed());
  }

  getUserInitials(): string {
    const u = this.currentUser;
    if (!u) return '?';
    return (u.firstName?.charAt(0) || '') + (u.lastName?.charAt(0) || '');
  }

  logout(): void {
    this.authService.logout();
  }
}
