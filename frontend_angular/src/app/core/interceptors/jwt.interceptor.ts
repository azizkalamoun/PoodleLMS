import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();

  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // Only logout if we have a token and are not on the login page
        // This prevents logging out during failed login attempts
        if (token && !router.url.includes('/login')) {
          console.warn(' [JWT-INTERCEPTOR] 401 error - Logging out');
          authService.logout();
        }
      }
      return throwError(() => error);
    }),
  );
};
