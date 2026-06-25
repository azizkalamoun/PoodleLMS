import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { EnrollmentService } from '../../core/services/enrollment.service';
import { CertificateService } from '../../core/services/certificate.service';
import { Enrollment, Certificate } from '../../models';
import { AuthService } from '../../core/services/auth.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { SkeletonComponent } from '../../shared/components/skeleton/skeleton.component';

@Component({
  selector: 'app-employee-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatIconModule,
    StatusBadgeComponent,
    PageHeaderComponent,
    EmptyStateComponent,
    SkeletonComponent,
  ],
  templateUrl: './employee-dashboard.component.html',
})
export class EmployeeDashboardComponent implements OnInit {
  currentUser = this.authService.getCurrentUser();
  enrollments: Enrollment[] = [];
  certificates: Certificate[] = [];
  loading = true;

  get passedCount(): number {
    return this.enrollments.filter((e) => e.status === 'PASSED').length;
  }

  get inProgressCount(): number {
    return this.enrollments.filter((e) => e.status === 'IN_PROGRESS').length;
  }

  get overdueCount(): number {
    return this.enrollments.filter((e) => e.status === 'OVERDUE').length;
  }

  getUserName(): string {
    return this.currentUser?.firstName || 'there';
  }

  constructor(
    private authService: AuthService,
    private enrollmentService: EnrollmentService,
    private certificateService: CertificateService,
  ) {}

  ngOnInit(): void {
    this.enrollmentService.getMyEnrollments().subscribe({
      next: (data) => {
        this.enrollments = data;
        this.loading = false;
      },
      error: () => (this.loading = false),
    });

    this.certificateService.getMyCertificates().subscribe({
      next: (data) => (this.certificates = data),
      error: () => console.error('Failed to load certificates'),
    });
  }
}
