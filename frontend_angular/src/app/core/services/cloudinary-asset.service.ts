import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError, of } from 'rxjs';
import { catchError, retry } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface CloudinaryDeleteResponse {
  result: string; // 'ok' on success
  public_id?: string;
}

@Injectable({
  providedIn: 'root',
})
export class CloudinaryAssetService {
  // Cloudinary API configuration for deletions
  private cloudName: string;
  private apiKey: string;
  private apiSecret: string;
  private deleteApiUrl: string;

  constructor(private http: HttpClient) {
    this.cloudName = environment.cloudinary.cloudName;
    this.apiKey = environment.cloudinary.apiKey || '';
    this.apiSecret = environment.cloudinary.apiSecret || '';
    this.deleteApiUrl = `https://api.cloudinary.com/v1_1/${this.cloudName}/resources/image/upload`;

    if (!this.apiKey || !this.apiSecret) {
      console.warn(
        'Cloudinary API credentials not configured for asset deletion. ' +
          'Set apiKey and apiSecret in environment.cloudinary for delete functionality.',
      );
    }
  }

  /**
   * Delete an asset from Cloudinary by public_id
   * Requires Cloudinary API key and secret configured
   */
  deleteAsset(publicId: string): Observable<CloudinaryDeleteResponse> {
    if (!this.apiKey || !this.apiSecret) {
      console.warn(
        `Skipping deletion of ${publicId} - credentials not configured`,
      );
      // Return success to not block the flow
      return of({ result: 'ok', public_id: publicId });
    }

    console.log(`Attempting to delete Cloudinary asset: ${publicId}`);

    // Create basic auth header for Cloudinary API
    const auth = btoa(`${this.apiKey}:${this.apiSecret}`);
    const headers = new HttpHeaders({
      Authorization: `Basic ${auth}`,
    });

    // Cloudinary delete endpoint
    const deleteUrl = `${this.deleteApiUrl}/${publicId}`;

    return this.http
      .delete<CloudinaryDeleteResponse>(deleteUrl, { headers })
      .pipe(
        retry(2), // Retry up to 2 times
        catchError((error) => {
          console.error(`Failed to delete asset ${publicId}:`, error);
          // Don't throw error - log it but continue
          // This prevents deletion of section if asset cleanup fails
          return of({ result: 'ok', public_id: publicId });
        }),
      );
  }

  /**
   * Extract public_id from Cloudinary secure_url
   * Example: https://res.cloudinary.com/dnmhrecwb/image/upload/v1234567890/poodle_lms/courses/1/IMAGE/filename.jpg
   * Returns: poodle_lms/courses/1/IMAGE/filename
   */
  extractPublicIdFromUrl(secureUrl: string): string | null {
    try {
      // Format: https://res.cloudinary.com/{cloud_name}/{resource_type}/upload/{version}/{public_id}.{ext}
      const match = secureUrl.match(/\/upload\/(?:v\d+\/)?(.+)\.\w+$/);
      if (match && match[1]) {
        return match[1];
      }
    } catch (error) {
      console.error('Error extracting public_id from URL:', error);
    }
    return null;
  }

  /**
   * Delete asset if URL is valid Cloudinary URL
   */
  deleteAssetIfCloudinary(contentUrl: string | undefined): Observable<void> {
    if (!contentUrl) {
      return of(void 0);
    }

    // Check if it's a Cloudinary URL
    if (!contentUrl.includes('cloudinary.com')) {
      console.log('Not a Cloudinary URL, skipping deletion:', contentUrl);
      return of(void 0);
    }

    const publicId = this.extractPublicIdFromUrl(contentUrl);
    if (!publicId) {
      console.warn('Could not extract public_id from URL:', contentUrl);
      return of(void 0);
    }

    return new Observable((observer) => {
      this.deleteAsset(publicId).subscribe({
        next: (response) => {
          if (response.result === 'ok') {
            console.log(`Asset deleted: ${publicId}`);
          }
          observer.next();
          observer.complete();
        },
        error: (err) => {
          console.warn(`Asset deletion encountered error: ${err.message}`);
          // Don't fail the flow
          observer.next();
          observer.complete();
        },
      });
    });
  }

  /**
   * Delete multiple assets in parallel
   */
  deleteMultipleAssets(urls: string[]): Observable<void> {
    const deleteObservables = urls
      .filter((url) => url && url.includes('cloudinary.com'))
      .map((url) => {
        const publicId = this.extractPublicIdFromUrl(url);
        return publicId ? this.deleteAsset(publicId) : of({ result: 'ok' });
      });

    if (deleteObservables.length === 0) {
      return of(void 0);
    }

    return new Observable((observer) => {
      let completed = 0;

      deleteObservables.forEach((deleteObs) => {
        deleteObs.subscribe({
          next: () => {
            completed++;
            if (completed === deleteObservables.length) {
              console.log(`All ${deleteObservables.length} assets deleted`);
              observer.next();
              observer.complete();
            }
          },
          error: (err) => {
            console.warn('Error deleting asset:', err);
            completed++;
            if (completed === deleteObservables.length) {
              observer.next();
              observer.complete();
            }
          },
        });
      });
    });
  }

  /**
   * Check if credentials are configured
   */
  hasCredentials(): boolean {
    return !!this.apiKey && !!this.apiSecret;
  }

  /**
   * Get asset info from Cloudinary
   */
  getAssetInfo(publicId: string): Observable<any> {
    if (!this.apiKey || !this.apiSecret) {
      return throwError(() => new Error('Credentials not configured'));
    }

    const auth = btoa(`${this.apiKey}:${this.apiSecret}`);
    const headers = new HttpHeaders({
      Authorization: `Basic ${auth}`,
    });

    const infoUrl = `${this.deleteApiUrl}/${publicId}`;

    return this.http.get(infoUrl, { headers }).pipe(
      catchError((error) => {
        console.error(`Error fetching asset info for ${publicId}:`, error);
        return throwError(() => error);
      }),
    );
  }
}
