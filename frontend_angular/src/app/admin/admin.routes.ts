import { Routes } from '@angular/router';
import { AdminLayoutComponent } from './admin-layout/admin-layout.component';
import { AdminDashboardComponent } from './dashboard/admin-dashboard.component';
import { AdminEmployeesComponent } from './employees/admin-employees.component';
import { EmployeeProgressComponent } from './employees/employee-progress/employee-progress.component';
import { AdminDepartmentsComponent } from './departments/admin-departments.component';
import { AdminCoursesComponent } from './courses/course-list/admin-courses.component';
import { AdminCourseDetailComponent } from './courses/course-detail/admin-course-detail.component';
import { AdminAnalyticsComponent } from './analytics/admin-analytics.component';
import { AdminCertificatesComponent } from './certificates/admin-certificates.component';

export const adminRoutes: Routes = [
  {
    path: '',
    component: AdminLayoutComponent,
    children: [
      { path: 'dashboard', component: AdminDashboardComponent },
      { path: 'employees', component: AdminEmployeesComponent },
      { path: 'employees/:id/progress', component: EmployeeProgressComponent },
      { path: 'departments', component: AdminDepartmentsComponent },
      { path: 'courses', component: AdminCoursesComponent },
      { path: 'courses/:id', component: AdminCourseDetailComponent },
      { path: 'certificates', component: AdminCertificatesComponent },
      { path: 'analytics', component: AdminAnalyticsComponent },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ],
  },
];
