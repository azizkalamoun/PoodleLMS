import { Component, OnInit, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import {
  MatDialog,
  MatDialogModule,
  MAT_DIALOG_DATA,
} from '@angular/material/dialog';
import { CertificateService } from '../../core/services/certificate.service';
import { AuthService } from '../../core/services/auth.service';
import { Certificate } from '../../models';
import { QrCodeComponent } from '../../shared/components/qr-code/qr-code.component';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { SkeletonComponent } from '../../shared/components/skeleton/skeleton.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-confirm-dialog',
  template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <mat-dialog-content>
      <p>{{ data.message }}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button mat-raised-button color="primary" [mat-dialog-close]="true">
        Confirm
      </button>
    </mat-dialog-actions>
  `,
  standalone: true,
  imports: [MatDialogModule, MatButtonModule],
})
export class ConfirmDialog {
  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {}
}

@Component({
  selector: 'app-employee-certificates',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    MatButtonModule,
    MatDialogModule,
    QrCodeComponent,
    PageHeaderComponent,
    SkeletonComponent,
    EmptyStateComponent,
    ConfirmDialog,
  ],
  templateUrl: './employee-certificates.component.html',
})
export class EmployeeCertificatesComponent implements OnInit {
  certificates: Certificate[] = [];
  loading = true;
  actionInProgress: { [key: number]: boolean } = {};
  isAdmin = false;

  constructor(
    private certificateService: CertificateService,
    private authService: AuthService,
    private dialog: MatDialog,
  ) {
    this.isAdmin = this.authService.isAdmin();
  }

  ngOnInit(): void {
    this.certificateService.getMyCertificates().subscribe({
      next: (data) => {
        this.certificates = data;
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
  }

  getVerificationUrl(cert: Certificate): string {
    return `${window.location.origin}/verify/${cert.verificationCode}`;
  }

  unrevokeCertificate(cert: Certificate): void {
    if (!this.isAdmin) {
      return;
    }

    const dialogRef = this.dialog.open(ConfirmDialog, {
      width: '400px',
      data: {
        title: 'Restore Certificate',
        message: `Are you sure you want to restore this certificate? It will be marked as valid again.`,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.actionInProgress[cert.id] = true;
        this.certificateService.unrevokeCertificate(cert.id).subscribe({
          next: (updatedCert) => {
            const index = this.certificates.findIndex((c) => c.id === cert.id);
            if (index !== -1) {
              this.certificates[index] = updatedCert;
            }
            this.actionInProgress[cert.id] = false;
          },
          error: () => {
            this.actionInProgress[cert.id] = false;
          },
        });
      }
    });
  }

  revokeCertificate(cert: Certificate): void {
    if (!this.isAdmin) {
      return;
    }

    const dialogRef = this.dialog.open(ConfirmDialog, {
      width: '400px',
      data: {
        title: 'Revoke Certificate',
        message: `Are you sure you want to revoke this certificate? It will be marked as invalid and cannot be verified.`,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.actionInProgress[cert.id] = true;
        this.certificateService.revokeCertificate(cert.id).subscribe({
          next: (updatedCert) => {
            const index = this.certificates.findIndex((c) => c.id === cert.id);
            if (index !== -1) {
              this.certificates[index] = updatedCert;
            }
            this.actionInProgress[cert.id] = false;
          },
          error: () => {
            this.actionInProgress[cert.id] = false;
          },
        });
      }
    });
  }
}
