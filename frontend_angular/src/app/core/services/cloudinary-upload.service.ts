import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface UploadProgress {
  progress: number; // 0-100
  loaded: number; // bytes loaded
  total: number; // total bytes
}

interface CloudinaryResponse {
  public_id: string;
  secure_url: string;
  url: string;
  width?: number;
  height?: number;
  size: number;
  resource_type: string;
}

@Injectable({
  providedIn: 'root',
})
export class CloudinaryUploadService {
  // Cloudinary configuration
  private cloudinaryUrl: string;
  private cloudinaryUploadPreset: string;

  // File size limits (in bytes)
  // Note: Individual chunk limit is 10MB (Cloudinary free tier)
  // Chunked upload service handles files larger than this automatically
  private readonly MAX_SINGLE_UPLOAD = 10 * 1024 * 1024; // 10 MB (Cloudinary free limit for single uploads)
  private readonly WARN_FILE_SIZE = 100 * 1024 * 1024; // 100 MB (warning threshold for very large files)
  private readonly MAX_TOTAL_FILE_SIZE = 500 * 1024 * 1024; // 500 MB absolute maximum

  constructor(private http: HttpClient) {
    // Load from environment
    this.cloudinaryUploadPreset = environment.cloudinary.uploadPreset;
    // Base URL without resource type - will be added per request
    this.cloudinaryUrl =
      environment.cloudinary.apiUrl.split('/image/upload')[0] ||
      environment.cloudinary.apiUrl;
  }

  /**
   * Build Cloudinary upload URL based on resource type
   * NOTE: PDFs/documents are uploaded to /image/upload instead of /raw/upload
   * because the account is marked as untrusted and doesn't have /raw/upload access.
   * They are organized in a PDF folder for organization.
   */
  private getCloudinaryUploadUrl(resourceType: string): string {
    // Ensure we have a clean base URL
    const baseUrl = this.cloudinaryUrl
      .split('/image/upload')[0]
      .split('/video/upload')[0]
      .split('/raw/upload')[0];

    // Append the correct resource type endpoint
    if (resourceType === 'video') {
      return `${baseUrl}/video/upload`;
    } else if (resourceType === 'raw') {
      // WORKAROUND: Account is untrusted for /raw/upload
      // Upload PDFs to /image/upload endpoint instead (account allows this)
      // Files are organized in a /PDF folder for easy retrieval
      console.warn(
        'PDF upload: Using /image/upload endpoint (account restriction: /raw/upload disabled for untrusted accounts)',
      );
      return `${baseUrl}/image/upload`;
    } else {
      // Default to image upload
      return `${baseUrl}/image/upload`;
    }
  }

  /**
   * Validate file size before upload
   * Returns error message if file is too large, null if OK
   * Note: Files > 10MB will use chunked upload automatically
   */
  validateFileSize(file: File): string | null {
    // Absolute maximum limit (500MB)
    if (file.size > this.MAX_TOTAL_FILE_SIZE) {
      const sizeMB = (file.size / (1024 * 1024)).toFixed(2);
      const maxMB = (this.MAX_TOTAL_FILE_SIZE / (1024 * 1024)).toFixed(0);
      return `File too large: ${sizeMB}MB. Maximum allowed: ${maxMB}MB.`;
    }

    // Warn for very large files (but don't block)
    if (file.size > this.WARN_FILE_SIZE) {
      const sizeMB = (file.size / (1024 * 1024)).toFixed(2);
      console.warn(
        `Very large file: ${sizeMB}MB. Upload will use chunked transfer and may take several minutes.`,
      );
    }

    return null;
  }

  /**
   * Get human-readable file size
   */
  getFileSizeDisplay(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
  }

  /**
   * Determine Cloudinary resource type based on file extension and MIME type
   * Cloudinary supports: 'image', 'video', 'raw', 'auto'
   */
  private getResourceType(fileName: string, mimeType: string): string {
    // Check MIME type first (more reliable)
    if (mimeType.startsWith('video/')) return 'video';
    if (mimeType.startsWith('audio/')) return 'video'; // Cloudinary treats audio as video resource
    if (mimeType.startsWith('image/')) return 'image';
    if (mimeType === 'application/pdf') return 'raw';

    // Fallback to file extension
    const ext = fileName.split('.').pop()?.toLowerCase() || '';
    const videoExtensions = [
      'mp4',
      'avi',
      'mov',
      'mkv',
      'flv',
      'wmv',
      'webm',
      'ogv',
      'ts',
      'mpg',
      'mpeg',
      'm4v',
      '3gp',
      '3g2',
      'mxf',
      'mts',
      'vob',
      'f4v',
    ];
    const audioExtensions = ['mp3', 'wav', 'aac', 'flac', 'ogg', 'm4a', 'wma'];
    const imageExtensions = [
      'jpg',
      'jpeg',
      'png',
      'gif',
      'webp',
      'svg',
      'bmp',
      'tiff',
    ];
    const rawExtensions = [
      'pdf',
      'doc',
      'docx',
      'xls',
      'xlsx',
      'ppt',
      'pptx',
      'txt',
    ];

    if (videoExtensions.includes(ext)) return 'video';
    if (audioExtensions.includes(ext)) return 'video'; // Cloudinary treats audio as video
    if (imageExtensions.includes(ext)) return 'image';
    if (rawExtensions.includes(ext)) return 'raw';

    // Default to 'auto' if we can't determine
    return 'auto';
  }

  /**
   * Upload a file to Cloudinary
   * Returns Observable with the uploaded file URL
   */
  uploadFile(file: File, path: string): Observable<string> {
    return this.uploadToCloudinary(file, path).pipe(
      map((response: CloudinaryResponse) => response.secure_url),
      catchError((error) => {
        console.error('Cloudinary upload error:', error);
        throw error;
      }),
    );
  }

  /**
   * Upload a file with progress tracking
   * Returns Observable that emits progress updates, then the final URL
   */
  uploadFileWithProgress(
    file: File,
    path: string,
  ): Observable<UploadProgress | string> {
    return new Observable((observer) => {
      const formData = new FormData();
      formData.append('file', file);

      // Determine resource type for proper handling
      const resourceType = this.getResourceType(file.name, file.type);

      // ALWAYS append the upload preset - it's required even for unsigned uploads
      // Cloudinary requires: upload_preset parameter for authentication/configuration
      formData.append('upload_preset', this.cloudinaryUploadPreset);

      formData.append('folder', path); // Store in Cloudinary folder structure

      // Get the correct URL based on resource type
      const uploadUrl = this.getCloudinaryUploadUrl(resourceType);

      console.log('Starting Cloudinary upload:', {
        fileName: file.name,
        fileSize: file.size,
        folder: path,
        resourceType,
        uploadUrl,
        uploadPreset:
          resourceType === 'video' ? 'unsigned' : this.cloudinaryUploadPreset,
      });

      const xhr = new XMLHttpRequest(); // Track upload progress
      xhr.upload.addEventListener('progress', (event) => {
        if (event.lengthComputable) {
          const progress = Math.round((event.loaded / event.total) * 100);
          console.log('Upload progress:', progress + '%', {
            loaded: event.loaded,
            total: event.total,
          });

          observer.next({
            progress,
            loaded: event.loaded,
            total: event.total,
          });
        }
      });

      // Handle completion
      xhr.addEventListener('load', () => {
        console.log('XHR Status:', xhr.status);
        if (xhr.status === 200) {
          const response = JSON.parse(xhr.responseText) as CloudinaryResponse;
          console.log('Upload finished, URL:', response.secure_url);
          observer.next(response.secure_url);
          observer.complete();
        } else {
          console.error('Upload failed - Response:', xhr.responseText);
          observer.error(
            new Error(
              `Upload failed with status ${xhr.status}: ${xhr.responseText}`,
            ),
          );
        }
      });

      // Handle errors
      xhr.addEventListener('error', () => {
        console.error('XHR error:', xhr.statusText);
        console.error('Response:', xhr.responseText);
        observer.error(new Error('Upload failed: ' + xhr.statusText));
      });

      xhr.addEventListener('abort', () => {
        console.log('Upload aborted');
        observer.error(new Error('Upload aborted'));
      });

      // Send the request - XMLHttpRequest automatically sets Content-Type for FormData
      xhr.open('POST', uploadUrl);
      xhr.send(formData);
    });
  }

  /**
   * Upload to Cloudinary using FormData
   */
  private uploadToCloudinary(
    file: File,
    path: string,
  ): Observable<CloudinaryResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('upload_preset', this.cloudinaryUploadPreset);
    formData.append('folder', path);

    return this.http.post<CloudinaryResponse>(this.cloudinaryUrl, formData);
  }

  /**
   * Generate upload path for organizing files in Cloudinary
   * PDFs are placed in a PDF subfolder for organization
   */
  getUploadPath(
    courseId: number,
    sectionType: string,
    fileName: string,
  ): string {
    // If this is a PDF, add PDF subfolder
    const ext = fileName.split('.').pop()?.toLowerCase() || '';
    if (ext === 'pdf') {
      return `poodle_lms/courses/${courseId}/PDF`;
    }
    return `poodle_lms/courses/${courseId}/${sectionType}`;
  }
}
