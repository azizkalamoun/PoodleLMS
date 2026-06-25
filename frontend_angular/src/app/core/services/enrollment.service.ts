import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Enrollment, QcmAttempt, QcmAttemptResult } from '../../models';

@Injectable({
  providedIn: 'root',
})
export class EnrollmentService {
  private apiUrl = `${environment.apiUrl}/enrollments`;

  constructor(private http: HttpClient) {}

  getMyEnrollments(): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(`${this.apiUrl}/my`);
  }

  getEmployeeEnrollments(employeeId: number): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(`${this.apiUrl}/employee/${employeeId}`);
  }

  getEnrollmentById(id: number): Observable<Enrollment> {
    return this.http.get<Enrollment>(`${this.apiUrl}/${id}`);
  }

  getByCourse(courseId: number): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(`${this.apiUrl}/course/${courseId}`);
  }

  submitPracticeAttempt(
    enrollmentId: number,
    attempt: QcmAttempt,
  ): Observable<QcmAttemptResult> {
    return this.http.post<QcmAttemptResult>(
      `${this.apiUrl}/${enrollmentId}/practice-attempt`,
      attempt,
    );
  }

  submitFinalAttempt(
    enrollmentId: number,
    attempt: QcmAttempt,
  ): Observable<QcmAttemptResult> {
    return this.http.post<QcmAttemptResult>(
      `${this.apiUrl}/${enrollmentId}/final-attempt`,
      attempt,
    );
  }
}
