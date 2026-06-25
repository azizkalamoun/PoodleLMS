import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, User } from '../../models';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {
    this.loadUserFromStorage();
  }

  private loadUserFromStorage(): void {
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');
    console.log('Loading from storage - Token exists:', !!token);
    if (token && user) {
      try {
        if (user === 'undefined' || user === 'null' || user.trim() === '') {
          throw new Error(`Invalid user value in storage: "${user}"`);
        }
        const parsedUser = JSON.parse(user);
        console.log('[AUTH-SERVICE] User loaded from storage:', parsedUser);
        this.currentUserSubject.next(parsedUser);
      } catch (e) {
        console.error('[AUTH-SERVICE] Failed to parse user from storage:', e);
        this.logout();
      }
    }
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    console.log('[AUTH-SERVICE] Sending login request for:', credentials.email);
    return this.http
      .post<LoginResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap((response) => {
          console.log('[AUTH-SERVICE] Login response received:', response);
          console.log(
            '[AUTH-SERVICE] Token received:',
            response.token ? 'Yes' : 'No',
          );
          console.log('[AUTH-SERVICE] User data in response:', {
            email: response.email,
            role: response.role,
            firstName: response.firstName,
            lastName: response.lastName,
          });

          const user: User = {
            // Backend doesn't provide id in AuthResponse; keep a safe placeholder.
            id: 0,
            email: response.email,
            role: response.role,
            firstName: response.firstName,
            lastName: response.lastName,
          };

          localStorage.setItem('token', response.token);
          console.log('✓ [AUTH-SERVICE] Token saved to localStorage');

          localStorage.setItem('user', JSON.stringify(user));
          console.log('✓ [AUTH-SERVICE] User saved to localStorage');

          this.currentUserSubject.next(user);
          console.log('[AUTH-SERVICE] Current user subject updated:', user);
        }),
      );
  }

  logout(): void {
    console.log('[AUTH-SERVICE] Logging out...');
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.currentUserSubject.next(null);
    console.log('[AUTH-SERVICE] Logout complete');
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    const token = localStorage.getItem('token');
    console.debug('🔑 [AUTH-SERVICE] getToken called - exists:', !!token);
    return token;
  }

  getCurrentUser(): User | null {
    const user = this.currentUserSubject.value;
    console.debug('👤 [AUTH-SERVICE] getCurrentUser called:', user);
    return user;
  }

  isLoggedIn(): boolean {
    const logged = !!this.getToken();
    console.debug('🔐 [AUTH-SERVICE] isLoggedIn:', logged);
    return logged;
  }

  hasRole(role: string): boolean {
    const user = this.getCurrentUser();
    const hasRole = user?.role === role;
    console.debug(
      '🔍 [AUTH-SERVICE] hasRole check - role:',
      role,
      'user role:',
      user?.role,
      'result:',
      hasRole,
    );
    return hasRole;
  }

  isAdmin(): boolean {
    return this.hasRole('ROLE_ADMIN');
  }

  isEmployee(): boolean {
    return this.hasRole('ROLE_EMPLOYEE');
  }
}
