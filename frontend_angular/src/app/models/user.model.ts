export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: 'ROLE_ADMIN' | 'ROLE_EMPLOYEE';
  departmentId?: number;
  departmentName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  // Backend returns a flat payload (no nested `user` object)
  email: string;
  firstName: string;
  lastName: string;
  role: 'ROLE_ADMIN' | 'ROLE_EMPLOYEE';
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  role: 'ROLE_ADMIN' | 'ROLE_EMPLOYEE';
  departmentId?: number;
}
