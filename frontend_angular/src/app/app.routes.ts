import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { LoginComponent } from './auth/login/login.component';
import { CertificateVerifyComponent } from './shared/components/certificate-verify/certificate-verify.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'verify/:code', component: CertificateVerifyComponent },
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard('ADMIN')],
    loadChildren: () =>
      import('./admin/admin.routes').then((m) => m.adminRoutes),
  },
  {
    path: 'employee',
    canActivate: [authGuard, roleGuard('EMPLOYEE')],
    loadChildren: () =>
      import('./employee/employee.routes').then((m) => m.employeeRoutes),
  },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' },
];
