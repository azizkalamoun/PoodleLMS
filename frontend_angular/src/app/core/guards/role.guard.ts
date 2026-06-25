import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const roleGuard = (requiredRole: string): CanActivateFn => {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isLoggedIn()) {
      router.navigate(['/login']);
      return false;
    }

    // Convert shorthand role to full role format (ADMIN -> ROLE_ADMIN, EMPLOYEE -> ROLE_EMPLOYEE)
    const fullRole = requiredRole.startsWith('ROLE_')
      ? requiredRole
      : `ROLE_${requiredRole}`;

    if (authService.hasRole(fullRole)) {
      return true;
    }

    // Redirect to appropriate dashboard
    if (authService.isAdmin()) {
      router.navigate(['/admin/dashboard']);
    } else {
      router.navigate(['/employee/dashboard']);
    }
    return false;
  };
};
