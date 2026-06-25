import { Component, OnInit, ViewChild, TemplateRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import {
  ReactiveFormsModule,
  FormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { CourseService } from '../../../core/services/course.service';
import { SnackBarService } from '../../../core/services/snackbar.service';
import { Course } from '../../../models';
import { ModalConfig } from '../../../core/utils/modal-config';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { SkeletonComponent } from '../../../shared/components/skeleton/skeleton.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-admin-courses',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDialogModule,
    MatPaginatorModule,
    SkeletonComponent,
    StatusBadgeComponent,
    PageHeaderComponent,
    EmptyStateComponent,
  ],
  templateUrl: './admin-courses.component.html',
})
export class AdminCoursesComponent implements OnInit {
  courses: Course[] = [];
  filteredCourses: Course[] = [];
  paginatedCourses: Course[] = [];
  courseForm!: FormGroup;
  editingId: number | null = null;
  loading = false;
  dialogRef: any = null;

  // Search and filter properties
  searchTerm = '';
  statusFilter = '';
  // Order: 'newest' (default) or 'oldest'
  order: 'newest' | 'oldest' = 'newest';

  // Pagination properties
  pageSize = 9;
  currentPage = 0;
  totalElements = 0;

  breadcrumbs = [
    { label: 'Admin', route: '/admin/dashboard' },
    { label: 'Courses' },
  ];

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild('courseFormTemplate') courseFormTemplate!: TemplateRef<any>;

  constructor(
    private courseService: CourseService,
    private fb: FormBuilder,
    private dialog: MatDialog,
    private notify: SnackBarService,
  ) {
    this.initForm();
  }

  ngOnInit(): void {
    this.loadCourses();
  }

  private initForm(): void {
    this.courseForm = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      status: ['DRAFT', Validators.required],
      passingScore: [
        70,
        [Validators.required, Validators.min(0), Validators.max(100)],
      ],
    });
  }

  onSearchChange(searchTerm: string): void {
    this.searchTerm = searchTerm.toLowerCase();
    // Reset to first page when searching
    this.currentPage = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.applyFilters();
  }

  onStatusFilterChange(status: string): void {
    this.statusFilter = status;
    // Reset to first page when filtering
    this.currentPage = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.applyFilters();
  }

  private applyFilters(): void {
    // Filter all loaded courses (not just current page)
    this.filteredCourses = this.courses.filter((course) => {
      const matchesSearch =
        !this.searchTerm ||
        course.title.toLowerCase().includes(this.searchTerm) ||
        course.description.toLowerCase().includes(this.searchTerm);

      const matchesStatus =
        !this.statusFilter || course.status === this.statusFilter;

      return matchesSearch && matchesStatus;
    });

    // Update total elements for pagination
    this.totalElements = this.filteredCourses.length;
    // Update paginated view
    this.updatePaginatedCourses();
  }

  private updatePaginatedCourses(): void {
    const startIndex = this.currentPage * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.paginatedCourses = this.filteredCourses.slice(startIndex, endIndex);
  }

  applyOrderCourses(): void {
    if (!this.courses) return;
    this.courses.sort((a, b) => {
      const getTime = (c: any) => {
        if (!c) return 0;
        if (c.createdAt) return new Date(c.createdAt).getTime();
        if (c.updatedAt) return new Date(c.updatedAt).getTime();
        if (c.id) return c.id;
        return 0;
      };

      const ta = getTime(a);
      const tb = getTime(b);
      if (this.order === 'newest') return tb - ta;
      return ta - tb;
    });
  }

  onPageChange(event: any): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.updatePaginatedCourses();
  }

  loadCourses(): void {
    this.loading = true;
    this.courseService.getAll(this.order).subscribe({
      next: (courses) => {
        this.courses = courses;
        this.applyOrderCourses();
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        const errorMsg =
          err.status === 409 ? err.message : 'Failed to load courses';
        this.notify.error(errorMsg);
      },
    });
  }

  onOrderChange(order: 'newest' | 'oldest'): void {
    this.order = order;
    this.currentPage = 0;
    this.loadCourses();
  }

  onSubmit(): void {
    if (this.courseForm.invalid) return;

    const data = this.courseForm.value;
    if (this.editingId) {
      this.courseService.update(this.editingId, data).subscribe({
        next: () => {
          this.notify.success('Course updated successfully!');
          this.loadCourses();
          if (this.dialogRef) {
            this.dialogRef.close(true);
          }
        },
        error: (err) => {
          // Handle 409 Conflict error
          if (err.status === 409) {
            this.notify.error(
              err.message ||
                'A course with this title already exists. Please use a different title.',
            );
          } else {
            this.notify.error(err.message || 'Failed to update course');
          }
        },
      });
    } else {
      this.courseService.create(data).subscribe({
        next: () => {
          this.notify.success('Course created successfully!');
          this.loadCourses();
          if (this.dialogRef) {
            this.dialogRef.close(true);
          }
        },
        error: (err) => {
          // Handle 409 Conflict error
          if (err.status === 409) {
            this.notify.error(
              err.message ||
                'A course with this title already exists. Please use a different title.',
            );
          } else {
            this.notify.error(err.message || 'Failed to create course');
          }
        },
      });
    }
  }

  editCourse(course: Course): void {
    this.editingId = course.id;
    this.courseForm.patchValue(course);

    this.dialogRef = this.dialog.open(
      this.courseFormTemplate,
      ModalConfig.getMediumDialogConfig(),
    );

    this.dialogRef.afterClosed().subscribe((result: any) => {
      this.dialogRef = null;
      if (result !== false) {
        this.resetForm();
      }
    });
  }

  deleteCourse(course: Course): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Delete Course', message: `Delete "${course.title}"?` },
    });
    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.courseService.delete(course.id).subscribe({
          next: () => {
            this.notify.success('Course deleted!');
            this.loadCourses();
          },
          error: () => this.notify.error('Delete failed'),
        });
      }
    });
  }

  resetForm(): void {
    this.editingId = null;
    this.initForm();
    this.searchTerm = '';
    this.statusFilter = '';
    this.currentPage = 0;
    this.applyFilters();
  }

  openAddCourseModal(): void {
    this.editingId = null;
    this.courseForm.reset({ status: 'DRAFT', passingScore: 70 });

    this.dialogRef = this.dialog.open(
      this.courseFormTemplate,
      ModalConfig.getMediumDialogConfig(),
    );

    this.dialogRef.afterClosed().subscribe((result: any) => {
      this.dialogRef = null;
      if (result !== false) {
        this.resetForm();
      }
    });
  }
}
