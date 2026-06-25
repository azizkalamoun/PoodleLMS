import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Department, DepartmentCreateRequest } from '../../models';

interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root',
})
export class DepartmentService {
  private apiUrl = `${environment.apiUrl}/departments`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Department[]> {
    return this.http
      .get<PageResponse<Department>>(this.apiUrl)
      .pipe(map((response) => response.content || []));
  }

  getTree(): Observable<Department[]> {
    return this.http.get<Department[]>(`${this.apiUrl}/tree`);
  }

  getById(id: number): Observable<Department> {
    return this.http.get<Department>(`${this.apiUrl}/${id}`);
  }

  create(department: DepartmentCreateRequest): Observable<Department> {
    // Map frontend field names to backend expected names
    const payload = {
      name: department.name,
      description: department.description,
      parentDepartmentId: department.parentId, // ← parentId → parentDepartmentId
    };
    return this.http.post<Department>(this.apiUrl, payload);
  }

  update(id: number, department: Partial<Department>): Observable<Department> {
    // Map frontend field names to backend expected names
    const payload = {
      name: department.name,
      description: department.description,
      parentDepartmentId: department.parentId, // ← parentId → parentDepartmentId
    };
    return this.http.put<Department>(`${this.apiUrl}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getChildren(id: number): Observable<Department[]> {
    return this.http.get<Department[]>(`${this.apiUrl}/${id}/children`);
  }
}
