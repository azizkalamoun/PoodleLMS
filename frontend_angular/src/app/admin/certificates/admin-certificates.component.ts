import { Component, OnInit, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  MatDialog,
  MatDialogModule,
  MAT_DIALOG_DATA,
} from '@angular/material/dialog';
import { CertificateService } from '../../core/services/certificate.service';
import { Certificate } from '../../models';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { SkeletonComponent } from '../../shared/components/skeleton/skeleton.component';

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
export class AdminCertificateConfirmDialog {
  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {}
}

@Component({
  selector: 'app-admin-certificates',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatIconModule,
    MatButtonModule,
    MatTableModule,
    MatPaginatorModule,
    MatTooltipModule,
    MatDialogModule,
    PageHeaderComponent,
    SkeletonComponent,
    AdminCertificateConfirmDialog,
  ],
  templateUrl: './admin-certificates.component.html',
})
export class AdminCertificatesComponent implements OnInit {
  certificates: Certificate[] = [];
  loading = true;
  actionInProgress: { [key: number]: boolean } = {};
  displayedColumns: string[] = [
    'employee',
    'course',
    'score',
    'issuedAt',
    'status',
    'actions',
  ];
  totalElements = 0;
  pageSize = 10;
  currentPage = 0;

  constructor(
    private certificateService: CertificateService,
    private dialog: MatDialog,
  ) {}

  ngOnInit(): void {
    this.loadCertificates();
  }

  loadCertificates(): void {
    this.loading = true;
    this.certificateService
      .getAllCertificates(this.currentPage, this.pageSize)
      .subscribe({
        next: (response) => {
          this.certificates = response.content || [];
          this.totalElements = response.totalElements || 0;
          this.loading = false;
        },
        error: () => (this.loading = false),
      });
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadCertificates();
  }

  getStatusBadgeClass(cert: Certificate): string {
    return cert.revoked
      ? 'bg-red-100 text-red-700'
      : 'bg-green-100 text-green-700';
  }

  getStatusText(cert: Certificate): string {
    return cert.revoked ? 'REVOKED' : 'VALID';
  }

  revokeCertificate(cert: Certificate): void {
    const dialogRef = this.dialog.open(AdminCertificateConfirmDialog, {
      width: '400px',
      data: {
        title: 'Revoke Certificate',
        message: `Are you sure you want to revoke this certificate for ${cert.employeeName}? It will be marked as invalid.`,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.actionInProgress[cert.id] = true;
        this.certificateService.revokeCertificate(cert.id).subscribe({
          next: () => {
            const index = this.certificates.findIndex((c) => c.id === cert.id);
            if (index !== -1) {
              this.certificates[index].revoked = true;
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

  unrevokeCertificate(cert: Certificate): void {
    const dialogRef = this.dialog.open(AdminCertificateConfirmDialog, {
      width: '400px',
      data: {
        title: 'Restore Certificate',
        message: `Are you sure you want to restore this certificate for ${cert.employeeName}? It will be marked as valid.`,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.actionInProgress[cert.id] = true;
        this.certificateService.unrevokeCertificate(cert.id).subscribe({
          next: () => {
            const index = this.certificates.findIndex((c) => c.id === cert.id);
            if (index !== -1) {
              this.certificates[index].revoked = false;
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
