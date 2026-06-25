import { Component, OnInit, ViewChild, TemplateRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTreeModule, MatTreeNestedDataSource } from '@angular/material/tree';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { NestedTreeControl } from '@angular/cdk/tree';
import { DepartmentService } from '../../core/services/department.service';
import { SnackBarService } from '../../core/services/snackbar.service';
import { Department } from '../../models';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { SkeletonComponent } from '../../shared/components/skeleton/skeleton.component';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ModalConfig } from '../../core/utils/modal-config';

@Component({
  selector: 'app-admin-departments',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTreeModule,
    MatDialogModule,
    MatTooltipModule,
    SkeletonComponent,
    PageHeaderComponent,
    EmptyStateComponent,
  ],
  templateUrl: './admin-departments.component.html',
})
export class AdminDepartmentsComponent implements OnInit {
  treeControl = new NestedTreeControl<Department>((node) => node.children);
  dataSource = new MatTreeNestedDataSource<Department>();
  departments: Department[] = [];
  flatDepartments: Department[] = [];
  deptForm!: FormGroup;
  editingId: number | null = null;
  parentId: number | null = null;
  loading = false;
  dialogRef: any = null;
  breadcrumbs = [
    { label: 'Admin', route: '/admin/dashboard' },
    { label: 'Departments' },
  ];

  @ViewChild('deptFormTemplate') deptFormTemplate!: TemplateRef<any>;

  constructor(
    private departmentService: DepartmentService,
    private fb: FormBuilder,
    private dialog: MatDialog,
    private notify: SnackBarService,
  ) {
    this.initForm();
  }

  ngOnInit(): void {
    this.loadDepartments();
  }

  hasChild = (_: number, node: Department) =>
    !!node.children && node.children.length > 0;

  private initForm(): void {
    this.deptForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      parentId: [this.parentId],
    });
  }

  loadDepartments(): void {
    this.loading = true;
    this.departmentService.getTree().subscribe({
      next: (tree) => {
        this.departments = tree;
        this.dataSource.data = tree;
        this.flattenDepartments(tree);
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        // Fallback to flat list
        this.departmentService.getAll().subscribe({
          next: (depts) => {
            this.departments = depts;
            this.dataSource.data = depts;
            this.flatDepartments = depts;
          },
          error: (flatErr) => {
            const errorMsg =
              flatErr.status === 409
                ? flatErr.message
                : 'Failed to load departments';
            this.notify.error(errorMsg);
          },
        });
      },
    });
  }

  private flattenDepartments(
    depts: Department[],
    result: Department[] = [],
  ): Department[] {
    for (const d of depts) {
      result.push(d);
      if (d.children) {
        this.flattenDepartments(d.children, result);
      }
    }
    this.flatDepartments = result;
    return result;
  }

  onSubmit(): void {
    if (this.deptForm.invalid) return;

    const data = this.deptForm.value;
    if (this.editingId) {
      this.departmentService.update(this.editingId, data).subscribe({
        next: () => {
          this.notify.success('Department updated successfully!');
          this.loadDepartments();
          if (this.dialogRef) {
            this.dialogRef.close(true);
          }
        },
        error: (err) => {
          // Handle 409 Conflict error
          if (err.status === 409) {
            this.notify.error(
              err.message ||
                'A department with this name already exists. Please use a different name.',
            );
          } else {
            this.notify.error(err.message || 'Failed to update department');
          }
        },
      });
    } else {
      this.departmentService.create(data).subscribe({
        next: () => {
          this.notify.success('Department created successfully!');
          this.loadDepartments();
          if (this.dialogRef) {
            this.dialogRef.close(true);
          }
        },
        error: (err) => {
          // Handle 409 Conflict error
          if (err.status === 409) {
            this.notify.error(
              err.message ||
                'A department with this name already exists. Please use a different name.',
            );
          } else {
            this.notify.error(err.message || 'Failed to create department');
          }
        },
      });
    }
  }

  addSubDepartment(parent: Department): void {
    this.parentId = parent.id;
    this.editingId = null;
    this.deptForm.reset({ parentId: parent.id });

    this.dialogRef = this.dialog.open(
      this.deptFormTemplate,
      ModalConfig.getSmallDialogConfig(),
    );

    this.dialogRef.afterClosed().subscribe((result: any) => {
      this.dialogRef = null;
      if (result !== false) {
        this.resetForm();
      }
    });
  }

  editDepartment(dept: Department): void {
    this.editingId = dept.id;
    this.parentId = null;
    this.deptForm.patchValue(dept);

    this.dialogRef = this.dialog.open(
      this.deptFormTemplate,
      ModalConfig.getSmallDialogConfig(),
    );

    this.dialogRef.afterClosed().subscribe((result: any) => {
      this.dialogRef = null;
      if (result !== false) {
        this.resetForm();
      }
    });
  }

  deleteDepartment(dept: Department): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete Department',
        message: `Are you sure you want to delete "${dept.name}"? All sub-departments will also be deleted.`,
      },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.departmentService.delete(dept.id).subscribe({
          next: () => {
            this.notify.success('Department deleted!');
            this.loadDepartments();
          },
          error: () => this.notify.error('Delete failed'),
        });
      }
    });
  }

  resetForm(): void {
    this.editingId = null;
    this.parentId = null;
    this.initForm();
  }

  openAddDepartmentModal(): void {
    this.editingId = null;
    this.parentId = null;
    this.deptForm.reset();

    this.dialogRef = this.dialog.open(
      this.deptFormTemplate,
      ModalConfig.getSmallDialogConfig(),
    );

    this.dialogRef.afterClosed().subscribe((result: any) => {
      this.dialogRef = null;
      if (result !== false) {
        this.resetForm();
      }
    });
  }
}
