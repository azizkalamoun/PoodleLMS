import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { EnrollmentService } from '../../../core/services/enrollment.service';
import { Enrollment } from '../../../models';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { SkeletonComponent } from '../../../shared/components/skeleton/skeleton.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-employee-courses',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatIconModule,
    FormsModule,
    MatFormFieldModule,
    MatSelectModule,
    PageHeaderComponent,
    SkeletonComponent,
    StatusBadgeComponent,
    EmptyStateComponent,
  ],
  templateUrl: './employee-courses.component.html',
})
export class EmployeeCoursesComponent implements OnInit {
  enrollments: Enrollment[] = [];
  loading = true;
  // ordering preference: newest (default) or oldest
  order: 'newest' | 'oldest' = 'newest';

  constructor(
    private enrollmentService: EnrollmentService,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit(): void {
    // Initialize order from query param or localStorage
    const qOrder = this.route.snapshot.queryParamMap.get('order');
    const stored = localStorage.getItem('employeeCoursesOrder');
    this.order = (qOrder as any) || (stored as any) || 'newest';

    this.loadEnrollments();
  }

  private loadEnrollments(): void {
    this.loading = true;
    this.enrollmentService.getMyEnrollments().subscribe({
      next: (data) => {
        this.enrollments = data || [];
        this.applyOrder();
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
  }

  onOrderChange(order: 'newest' | 'oldest'): void {
    this.order = order;
    // persist in localStorage as fallback
    try {
      localStorage.setItem('employeeCoursesOrder', order);
    } catch (e) {
      // ignore storage errors
    }
    // update route query param for shareable URL
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { order },
      queryParamsHandling: 'merge',
    });

    // reload and re-apply ordering (ensures fresh data and consistent ordering)
    this.loadEnrollments();
  }

  private applyOrder(): void {
    if (!this.enrollments) return;
    this.enrollments.sort((a, b) => {
      // prefer createdAt, fallback to updatedAt, then id
      const getTime = (e: any) => {
        if (!e) return 0;
        if (e.createdAt) return new Date(e.createdAt).getTime();
        if (e.updatedAt) return new Date(e.updatedAt).getTime();
        if (e.id) return e.id;
        return 0;
      };

      const ta = getTime(a);
      const tb = getTime(b);
      if (this.order === 'newest') return tb - ta;
      return ta - tb;
    });
  }

  get completedCount(): number {
    return this.enrollments.filter((e) => e.status === 'PASSED').length;
  }

  get inProgressCount(): number {
    return this.enrollments.filter((e) => e.status === 'IN_PROGRESS').length;
  }
}
