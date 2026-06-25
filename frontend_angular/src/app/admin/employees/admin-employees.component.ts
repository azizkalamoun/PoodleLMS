import { Component, OnInit, ViewChild, TemplateRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import {
  ReactiveFormsModule,
  FormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { EmployeeService } from '../../core/services/employee.service';
import { DepartmentService } from '../../core/services/department.service';
import { SnackBarService } from '../../core/services/snackbar.service';
import { User, Department } from '../../models';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { SkeletonComponent } from '../../shared/components/skeleton/skeleton.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ModalConfig } from '../../core/utils/modal-config';

@Component({
  selector: 'app-admin-employees',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDialogModule,
    MatTooltipModule,
    SkeletonComponent,
    StatusBadgeComponent,
    PageHeaderComponent,
    EmptyStateComponent,
  ],
  templateUrl: './admin-employees.component.html',
})
export class AdminEmployeesComponent implements OnInit {
  displayedColumns = ['id', 'name', 'email', 'role', 'department', 'actions'];
  dataSource = new MatTableDataSource<User>();
  allEmployees: User[] = [];
  departments: Department[] = [];
  employeeForm!: FormGroup;
  editingId: number | null = null;
  loading = false;
  totalElements = 0;
  pageSize = 10;
  currentPage = 0;
  selectedDeptFilter = 0;
  searchTerm = '';
  dialogRef: any = null;
  breadcrumbs = [
    { label: 'Admin', route: '/admin/dashboard' },
    { label: 'Employees' },
  ];

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild('employeeFormTemplate') employeeFormTemplate!: TemplateRef<any>;

  constructor(
    private employeeService: EmployeeService,
    private departmentService: DepartmentService,
    private fb: FormBuilder,
    private dialog: MatDialog,
    private notify: SnackBarService,
    private router: Router,
  ) {
    this.initForm();
  }

  ngOnInit(): void {
    this.loadEmployees();
    this.loadDepartments();
  }

  private initForm(): void {
    this.employeeForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      role: ['ROLE_EMPLOYEE', Validators.required],
      departmentId: [null],
    });
  }

  loadEmployees(): void {
    this.loading = true;
    this.employeeService.getAll().subscribe({
      next: (response) => {
        const employees = response.content || response;
        if (employees && Array.isArray(employees)) {
          this.allEmployees = employees;
          this.dataSource.data = employees;
          this.dataSource.paginator = this.paginator;
          if (this.paginator) {
            this.paginator.firstPage();
          }
          this.applyFilters();
        } else {
          this.allEmployees = [];
          this.dataSource.data = [];
        }
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        const errorMsg =
          err.status === 409 ? err.message : 'Failed to load employees';
        this.notify.error(errorMsg);
      },
    });
  }

  onSearchChange(searchTerm: string): void {
    this.searchTerm = searchTerm.toLowerCase();
    this.currentPage = 0;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.applyFilters();
  }

  onDepartmentFilterChange(deptId: number): void {
    this.selectedDeptFilter = deptId;
    if (this.paginator) {
      this.paginator.firstPage();
    }
    this.applyFilters();
  }

  private applyFilters(): void {
    const filtered = this.allEmployees.filter((employee) => {
      const matchesSearch =
        !this.searchTerm ||
        `${employee.firstName} ${employee.lastName}`
          .toLowerCase()
          .includes(this.searchTerm) ||
        employee.email.toLowerCase().includes(this.searchTerm);

      const matchesDepartment =
        !this.selectedDeptFilter ||
        employee.departmentId === this.selectedDeptFilter;

      return matchesSearch && matchesDepartment;
    });

    this.dataSource.data = filtered;
    this.totalElements = filtered.length;
    if (this.paginator) {
      this.paginator.firstPage();
    }
  }

  loadDepartments(): void {
    this.departmentService.getAll().subscribe({
      next: (depts) => (this.departments = depts),
      error: () => this.notify.error('Failed to load departments'),
    });
  }

  onSubmit(): void {
    if (this.employeeForm.invalid) return;

    const data = this.employeeForm.value;
    if (this.editingId) {
      const { password, role, ...updateData } = data;
      this.employeeService.update(this.editingId, updateData).subscribe({
        next: () => {
          this.notify.success('Employee updated successfully!');
          this.loadEmployees();
          if (this.dialogRef) {
            this.dialogRef.close(true);
          }
        },
        error: (err) => {
          if (err.status === 409 || (err.error?.message?.toLowerCase().includes('duplicate') || err.error?.message?.toLowerCase().includes('already exists'))) {
            this.notify.error(err.error?.message || 'This email is already in use. Please use a different email.');
          } else {
            this.notify.error(err.error?.message || 'Failed to update employee');
          }
        },
      });
    } else {
      this.employeeService.create(data).subscribe({
        next: () => {
          this.notify.success('Employee created successfully!');
          this.loadEmployees();
          if (this.dialogRef) {
            this.dialogRef.close(true);
          }
        },
        error: (err) => {
          if (err.status === 409 || (err.error?.message?.toLowerCase().includes('duplicate') || err.error?.message?.toLowerCase().includes('already exists'))) {
            this.notify.error(err.error?.message || 'This email is already in use. Please use a different email.');
          } else if (err.status === 0) {
            this.notify.error('Network error - please check if the backend is running');
          } else {
            this.notify.error(err.error?.message || 'Failed to create employee');
          }
        },
      });
    }
  }

  editEmployee(employee: User): void {
    this.editingId = employee.id;
    this.employeeForm.patchValue(employee);
    this.employeeForm.get('password')?.clearValidators();
    this.employeeForm.get('password')?.updateValueAndValidity();

    this.dialogRef = this.dialog.open(
      this.employeeFormTemplate,
      ModalConfig.getMediumDialogConfig(),
    );

    this.dialogRef.afterClosed().subscribe((result: any) => {
      this.dialogRef = null;
      if (result !== false) {
        this.resetForm();
      }
    });
  }

  deleteEmployee(employee: User): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete Employee',
        message: `Are you sure you want to delete ${employee.firstName} ${employee.lastName}?`,
      },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.employeeService.delete(employee.id).subscribe({
          next: () => {
            this.notify.success('Employee deleted!');
            this.loadEmployees();
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
    this.applyFilters();
  }

  openAddEmployeeModal(): void {
    this.editingId = null;
    this.employeeForm.reset({
      firstName: '',
      lastName: '',
      email: '',
      password: '',
      role: 'ROLE_EMPLOYEE',
      departmentId: null,
    });

    this.dialogRef = this.dialog.open(
      this.employeeFormTemplate,
      ModalConfig.getMediumDialogConfig(),
    );

    this.dialogRef.afterClosed().subscribe((result: any) => {
      this.dialogRef = null;
      if (result !== false) {
        this.resetForm();
      }
    });
  }

  viewEmployeeProgress(employee: User): void {
    if (employee.id) {
      this.router.navigate(['/admin/employees', employee.id, 'progress']);
    }
  }
}
