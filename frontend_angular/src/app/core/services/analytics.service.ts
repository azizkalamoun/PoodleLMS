import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AnalyticsOverview,
  CourseAnalytics,
  OverdueEnrollment,
  FailedQuestion,
  DepartmentAnalytics,
} from '../../models';

@Injectable({
  providedIn: 'root',
})
export class AnalyticsService {
  private apiUrl = `${environment.apiUrl}/analytics`;

  constructor(private http: HttpClient) {}

  // Dashboard overview
  getOverview(): Observable<AnalyticsOverview> {
    return this.http.get<AnalyticsOverview>(`${this.apiUrl}/overview`);
  }

  // All courses analytics
  getCourseAnalytics(): Observable<CourseAnalytics[]> {
    return this.http.get<CourseAnalytics[]>(`${this.apiUrl}/courses`);
  }

  // All overdue enrollments
  getOverdueEnrollments(): Observable<OverdueEnrollment[]> {
    return this.http.get<OverdueEnrollment[]>(`${this.apiUrl}/overdue`);
  }

  // Most failed questions globally
  getMostFailedQuestions(): Observable<FailedQuestion[]> {
    return this.http.get<FailedQuestion[]>(`${this.apiUrl}/failed-questions`);
  }

  // All departments analytics
  getDepartmentAnalytics(): Observable<DepartmentAnalytics[]> {
    return this.http.get<DepartmentAnalytics[]>(`${this.apiUrl}/departments`);
  }

  // Course-specific analytics
  getCourseCompletionRate(courseId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/course/${courseId}/completion`);
  }

  getCoursePassRate(courseId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/course/${courseId}/pass-rate`);
  }

  getOverdueEmployees(courseId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/course/${courseId}/overdue`);
  }

  getMostFailedQuestionsByCourse(courseId: number): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.apiUrl}/course/${courseId}/most-failed-questions`,
    );
  }

  // Department-specific analytics
  getDepartmentAverageScore(departmentId: number): Observable<any> {
    return this.http.get<any>(
      `${this.apiUrl}/department/${departmentId}/average-score`,
    );
  }
}
