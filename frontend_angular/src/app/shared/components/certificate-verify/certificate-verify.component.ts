import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { CertificateService } from '../../../core/services/certificate.service';
import { SkeletonComponent } from '../skeleton/skeleton.component';

interface CertificateVerificationResponse {
  employeeName: string;
  courseTitle: string;
  issuedAt: string;
  status: string; // VALID or REVOKED
}

@Component({
  selector: 'app-certificate-verify',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule, SkeletonComponent],
  templateUrl: './certificate-verify.component.html',
  styleUrls: ['./certificate-verify.component.scss'],
})
export class CertificateVerifyComponent implements OnInit {
  certificate: CertificateVerificationResponse | null = null;
  loading = true;
  error: string | null = null;
  verificationCode: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private certificateService: CertificateService,
  ) {}

  ngOnInit(): void {
    this.verificationCode = this.route.snapshot.paramMap.get('code');
    if (!this.verificationCode) {
      this.error = 'No verification code provided';
      this.loading = false;
      return;
    }
    this.verifyCertificate();
  }

  private verifyCertificate(): void {
    if (!this.verificationCode) return;

    this.certificateService.verify(this.verificationCode).subscribe({
      next: (data: any) => {
        this.certificate = data;
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Certificate verification failed:', err);
        this.error =
          err?.error?.message ||
          'Certificate not found or is invalid. Please check the verification code.';
        this.loading = false;
      },
    });
  }

  goHome(): void {
    this.router.navigate(['/']);
  }

  get isValid(): boolean {
    return this.certificate?.status === 'VALID';
  }

  get isRevoked(): boolean {
    return this.certificate?.status === 'REVOKED';
  }
}
