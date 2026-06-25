import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Certificate } from '../../models';

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  empty: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class CertificateService {
  private apiUrl = `${environment.apiUrl}/certificates`;

  constructor(private http: HttpClient) {}

  getMyCertificates(
    page: number = 0,
    size: number = 10,
  ): Observable<Certificate[]> {
    return this.http
      .get<Page<Certificate>>(`${this.apiUrl}/my?page=${page}&size=${size}`)
      .pipe(map((response) => response.content || []));
  }

  getAllCertificates(
    page: number = 0,
    size: number = 10,
  ): Observable<Page<Certificate>> {
    return this.http.get<Page<Certificate>>(
      `${this.apiUrl}?page=${page}&size=${size}`,
    );
  }

  getById(id: number): Observable<Certificate> {
    return this.http.get<Certificate>(`${this.apiUrl}/${id}`);
  }

  verify(verificationCode: string): Observable<Certificate> {
    return this.http.get<Certificate>(
      `${this.apiUrl}/verify/${verificationCode}`,
    );
  }

  revokeCertificate(id: number): Observable<Certificate> {
    return this.http.put<Certificate>(`${this.apiUrl}/${id}/revoke`, {});
  }

  unrevokeCertificate(id: number): Observable<Certificate> {
    return this.http.put<Certificate>(`${this.apiUrl}/${id}/unrevoke`, {});
  }
}
