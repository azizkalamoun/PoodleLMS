import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { EnrollmentService } from '../../../core/services/enrollment.service';
import { EmployeeService } from '../../../core/services/employee.service';
import { CourseService } from '../../../core/services/course.service';
import { SnackBarService } from '../../../core/services/snackbar.service';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { SkeletonComponent } from '../../../shared/components/skeleton/skeleton.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { User, Enrollment, Course } from '../../../models';

@Component({
  selector: 'app-employee-progress',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatProgressBarModule,
    MatTooltipModule,
    MatChipsModule,
    PageHeaderComponent,
    StatusBadgeComponent,
    SkeletonComponent,
    EmptyStateComponent,
  ],
  templateUrl: './employee-progress.component.html',
  styleUrls: ['./employee-progress.component.scss'],
})
export class EmployeeProgressComponent implements OnInit {
  employeeId: number = 0;
  employee: User | null = null;
  enrollments: Enrollment[] = [];
  dataSource = new MatTableDataSource<Enrollment>();
  displayedColumns = [
    'courseTitle',
    'status',
    'score',
    'attempts',
    'deadline',
    'completedAt',
  ];
  loading = true;
  courseMap = new Map<number, Course>();

  // Statistics
  totalCourses = 0;
  passedCourses = 0;
  failedCourses = 0;

  breadcrumbs = [
    { label: 'Admin', route: '/admin/dashboard' },
    { label: 'Employees', route: '/admin/employees' },
    { label: 'Progress' },
  ];
  inProgressCourses = 0;
  overdueCount = 0;
  averageScore = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private enrollmentService: EnrollmentService,
    private employeeService: EmployeeService,
    private courseService: CourseService,
    private notify: SnackBarService,
  ) {}

  ngOnInit(): void {
    this.loadEmployeeProgress();
  }

  loadEmployeeProgress(): void {
    this.route.params.subscribe((params) => {
      this.employeeId = +params['id'];
      if (this.employeeId) {
        this.loadEmployee();
        this.loadEnrollments();
      }
    });
  }

  loadEmployee(): void {
    this.employeeService.getById(this.employeeId).subscribe({
      next: (employee) => {
        this.employee = employee;
      },
      error: (err) => {
        console.error('Error loading employee:', err);
        this.notify.error('Failed to load employee details');
      },
    });
  }

  loadEnrollments(): void {
    // Get enrollments for the specific employee using the new admin endpoint
    this.enrollmentService.getEmployeeEnrollments(this.employeeId).subscribe({
      next: (enrollments) => {
        this.enrollments = enrollments;
        this.dataSource.data = this.enrollments;
        this.calculateStatistics();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading enrollments:', err);
        this.notify.error('Failed to load course progress');
        this.loading = false;
      },
    });
  }

  calculateStatistics(): void {
    this.totalCourses = this.enrollments.length;
    this.passedCourses = this.enrollments.filter(
      (e) => e.status === 'PASSED',
    ).length;
    this.failedCourses = this.enrollments.filter(
      (e) => e.status === 'FAILED',
    ).length;
    this.inProgressCourses = this.enrollments.filter(
      (e) => e.status === 'IN_PROGRESS',
    ).length;
    this.overdueCount = this.enrollments.filter(
      (e) => e.status === 'OVERDUE',
    ).length;

    const scoresArray = this.enrollments
      .filter((e) => e.score !== null && e.score !== undefined)
      .map((e) => e.score as number);
    this.averageScore =
      scoresArray.length > 0
        ? Math.round(
            (scoresArray.reduce((a, b) => a + b, 0) / scoresArray.length) * 100,
          ) / 100
        : 0;
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'PASSED':
        return 'text-green-600';
      case 'FAILED':
        return 'text-red-600';
      case 'IN_PROGRESS':
        return 'text-brand-600';
      case 'OVERDUE':
        return 'text-orange-600';
      default:
        return 'text-surface-600';
    }
  }

  getStatusBgColor(status: string): string {
    switch (status) {
      case 'PASSED':
        return 'bg-green-50';
      case 'FAILED':
        return 'bg-red-50';
      case 'IN_PROGRESS':
        return 'bg-brand-50';
      case 'OVERDUE':
        return 'bg-orange-50';
      default:
        return 'bg-surface-50';
    }
  }

  getProgressValue(enrollment: Enrollment): number {
    if (enrollment.status === 'PASSED') {
      return 100;
    } else if (enrollment.status === 'FAILED') {
      return 0;
    }
    return enrollment.score || 0;
  }

  goBack(): void {
    this.router.navigate(['/admin/employees']);
  }
}
