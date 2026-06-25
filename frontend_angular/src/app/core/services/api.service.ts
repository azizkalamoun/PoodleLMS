import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpParams,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

/**
 * Centralized API service for all HTTP requests
 * Handles:
 * - Base URL management
 * - HTTP methods (GET, POST, PUT, DELETE)
 * - Error handling
 * - Request/Response logging
 */
@Injectable({
  providedIn: 'root',
})
export class ApiService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  /**
   * GET request
   * @param endpoint API endpoint (e.g., '/courses', '/me')
   * @param params Optional query parameters
   */
  get<T>(
    endpoint: string,
    params?: { [key: string]: string | number },
  ): Observable<T> {
    let httpParams = new HttpParams();
    if (params) {
      Object.keys(params).forEach((key) => {
        httpParams = httpParams.set(key, params[key].toString());
      });
    }

    return this.http
      .get<T>(`${this.baseUrl}${endpoint}`, { params: httpParams })
      .pipe(catchError(this.handleError));
  }

  /**
   * POST request
   * @param endpoint API endpoint
   * @param body Request body
   */
  post<T>(endpoint: string, body: any = {}): Observable<T> {
    return this.http
      .post<T>(`${this.baseUrl}${endpoint}`, body)
      .pipe(catchError(this.handleError));
  }

  /**
   * PUT request
   * @param endpoint API endpoint
   * @param body Request body
   */
  put<T>(endpoint: string, body: any = {}): Observable<T> {
    return this.http
      .put<T>(`${this.baseUrl}${endpoint}`, body)
      .pipe(catchError(this.handleError));
  }

  /**
   * PATCH request
   * @param endpoint API endpoint
   * @param body Request body
   */
  patch<T>(endpoint: string, body: any = {}): Observable<T> {
    return this.http
      .patch<T>(`${this.baseUrl}${endpoint}`, body)
      .pipe(catchError(this.handleError));
  }

  /**
   * DELETE request
   * @param endpoint API endpoint
   */
  delete<T>(endpoint: string): Observable<T> {
    return this.http
      .delete<T>(`${this.baseUrl}${endpoint}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Handle HTTP errors
   * Logs error and returns Observable error with status code
   */
  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An error occurred';
    let statusCode = error.status;

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side error
      if (error.error && typeof error.error === 'object') {
        if (error.error.message) {
          errorMessage = error.error.message;
        } else if (error.error.detail) {
          errorMessage = error.error.detail;
        } else {
          errorMessage = `Error Code: ${error.status} - ${error.statusText}`;
        }
      } else {
        errorMessage = error.message || `Error Code: ${error.status}`;
      }

      // Handle specific status codes
      switch (statusCode) {
        case 409:
          errorMessage =
            error.error?.message ||
            'Conflict: Resource already exists or constraint violated';
          break;
        case 400:
          errorMessage =
            error.error?.message || 'Bad Request: Invalid data provided';
          break;
        case 401:
          errorMessage = 'Unauthorized: Please login again';
          break;
        case 403:
          errorMessage =
            'Forbidden: You do not have permission to perform this action';
          break;
        case 404:
          errorMessage = 'Not Found: Resource does not exist';
          break;
        case 500:
          errorMessage = 'Server Error: Please try again later';
          break;
      }
    }

    console.error(`[HTTP ${statusCode}] ${errorMessage}`);

    // Return error with status code attached
    const httpError = new Error(errorMessage) as any;
    httpError.status = statusCode;
    return throwError(() => httpError);
  }
}
