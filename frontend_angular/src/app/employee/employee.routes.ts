import { Routes } from '@angular/router';
import { EmployeeLayoutComponent } from './employee-layout/employee-layout.component';
import { EmployeeDashboardComponent } from './dashboard/employee-dashboard.component';
import { EmployeeCoursesComponent } from './courses/course-list/employee-courses.component';
import { EmployeeCourseDetailComponent } from './courses/course-detail/employee-course-detail.component';
import { EmployeeCertificatesComponent } from './certificates/employee-certificates.component';

export const employeeRoutes: Routes = [
  {
    path: '',
    component: EmployeeLayoutComponent,
    children: [
      { path: 'dashboard', component: EmployeeDashboardComponent },
      { path: 'courses', component: EmployeeCoursesComponent },
      { path: 'courses/:id', component: EmployeeCourseDetailComponent },
      { path: 'certificates', component: EmployeeCertificatesComponent },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ],
  },
];
