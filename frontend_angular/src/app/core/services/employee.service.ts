import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User, RegisterRequest } from '../../models';

@Injectable({
  providedIn: 'root',
})
export class EmployeeService {
  private apiUrl = `${environment.apiUrl}/employees`;

  constructor(private http: HttpClient) {}

  getAll(
    page: number = 0,
    size: number = 100,
    departmentId?: number,
  ): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (departmentId) {
      params = params.set('departmentId', departmentId.toString());
    }
    return this.http.get<any>(this.apiUrl, { params });
  }

  getById(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`);
  }

  create(employee: RegisterRequest): Observable<User> {
    return this.http.post<User>(this.apiUrl, employee);
  }

  update(id: number, employee: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}`, employee);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getByDepartment(departmentId: number): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/department/${departmentId}`);
  }
}
