import { Injectable } from '@angular/core';
import { Observable, Subject, throwError, of } from 'rxjs';
import {
  mergeMap,
  finalize,
  retry,
  catchError,
  concatMap,
} from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface ChunkUploadProgress {
  totalProgress: number; // 0-100 for overall upload
  currentChunk: number; // 1-based chunk number
  totalChunks: number;
  chunkProgress: number; // 0-100 for current chunk
  uploadedBytes: number;
  totalBytes: number;
  estimatedTimeRemaining?: number; // in seconds
}

interface CloudinaryChunkResponse {
  public_id: string;
  secure_url: string;
  url: string;
  resource_type: string;
  size: number;
}

@Injectable({
  providedIn: 'root',
})
export class ChunkedUploadService {
  // Configuration
  private readonly CHUNK_SIZE = 5 * 1024 * 1024; // 5 MB chunks
  private readonly MAX_CONCURRENT_CHUNKS = 3; // Upload 3 chunks in parallel
  private readonly RETRY_ATTEMPTS = 3; // Retry failed chunks
  private readonly RETRY_DELAY = 1000; // 1 second between retries

  private cloudinaryUrl: string;
  private cloudinaryUploadPreset: string;

  private uploadStartTime = 0;
  private uploadedBytes = 0;

  constructor() {
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
      console.warn(
        'PDF chunked upload: Using /image/upload endpoint (account restriction: /raw/upload disabled for untrusted accounts)',
      );
      return `${baseUrl}/image/upload`;
    } else {
      // Default to image upload
      return `${baseUrl}/image/upload`;
    }
  }

  /**
   * Upload a file using chunks
   * Splits file into 5MB chunks and uploads up to 3 concurrently
   * Returns Observable emitting progress updates, then final URL
   */
  uploadFileChunked(
    file: File,
    path: string,
  ): Observable<ChunkUploadProgress | string> {
    return new Observable((observer) => {
      const chunks = this.splitFileIntoChunks(file);
      const totalChunks = chunks.length;
      let completedChunks = 0;

      // Determine resource type from file
      const resourceType = this.getResourceType(file.name, file.type);

      console.log('Starting chunked upload:', {
        fileName: file.name,
        fileSize: file.size,
        totalChunks,
        chunkSize: this.CHUNK_SIZE,
        maxConcurrentChunks: this.MAX_CONCURRENT_CHUNKS,
        resourceType,
      });

      if (totalChunks === 0) {
        observer.error(new Error('File is empty'));
        return;
      }

      // If file fits in one chunk, upload directly without chunking overhead
      if (totalChunks === 1) {
        console.log('Single chunk upload (file < 5MB)');
        this.uploadSingleChunk(
          chunks[0],
          file.name,
          path,
          1,
          1,
          resourceType,
        ).subscribe({
          next: (result) => {
            if (typeof result === 'string') {
              observer.next(result);
              observer.complete();
            } else {
              observer.next(result);
            }
          },
          error: (err) => observer.error(err),
        });
        return;
      }

      // Multi-chunk upload with concurrency control
      this.uploadChunksWithConcurrency(
        chunks,
        file.name,
        path,
        totalChunks,
        observer,
        resourceType,
      );
    });
  }

  /**
   * Split file into chunks
   */
  private splitFileIntoChunks(file: File): Blob[] {
    const chunks: Blob[] = [];
    let offset = 0;

    while (offset < file.size) {
      const chunkEnd = Math.min(offset + this.CHUNK_SIZE, file.size);
      chunks.push(file.slice(offset, chunkEnd));
      offset = chunkEnd;
    }

    return chunks;
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
   * Upload chunks with concurrency control
   */
  private uploadChunksWithConcurrency(
    chunks: Blob[],
    fileName: string,
    path: string,
    totalChunks: number,
    observer: any,
    resourceType: string,
  ): void {
    const chunkStatuses = new Array(chunks.length).fill('pending'); // pending, uploading, completed, failed
    let completedChunks = 0;
    const chunkUrls: (string | null)[] = new Array(chunks.length).fill(null);

    this.uploadStartTime = Date.now();
    this.uploadedBytes = 0;

    // Create upload tasks for each chunk
    const uploadTasks = chunks.map((chunk, index) => {
      return of(index).pipe(
        mergeMap(
          (chunkIndex) =>
            new Observable<{ index: number; url: string }>((taskObserver) => {
              this.uploadSingleChunk(
                chunk,
                fileName,
                path,
                chunkIndex + 1,
                totalChunks,
                resourceType,
              ).subscribe({
                next: (result) => {
                  if (typeof result === 'string') {
                    // Final URL from single chunk
                    chunkUrls[chunkIndex] = result;
                    chunkStatuses[chunkIndex] = 'completed';
                    completedChunks++;

                    // Emit progress
                    this.emitProgress(
                      observer,
                      completedChunks,
                      totalChunks,
                      chunkIndex + 1,
                    );

                    taskObserver.next({ index: chunkIndex, url: result });
                    taskObserver.complete();
                  } else {
                    // Progress update
                    this.uploadedBytes +=
                      (result.chunkProgress || 0) * (this.CHUNK_SIZE / 100);
                    observer.next(result);
                  }
                },
                error: (err) => {
                  console.error(`Chunk ${chunkIndex + 1} failed:`, err);
                  taskObserver.error(err);
                },
              });
            }),
          this.MAX_CONCURRENT_CHUNKS, // Max 3 concurrent uploads
        ),
      );
    });

    // Process uploads sequentially with concurrency limit
    let activeUploads = 0;
    let completedIndex = 0;

    const processNextChunk = () => {
      if (completedIndex >= uploadTasks.length) {
        // All chunks completed
        observer.next('completed'); // Use chunked URL or reassemble if needed
        observer.complete();
        return;
      }

      if (activeUploads < this.MAX_CONCURRENT_CHUNKS) {
        activeUploads++;
        const currentIndex = completedIndex;
        completedIndex++;

        uploadTasks[currentIndex].subscribe({
          next: () => {
            activeUploads--;
            processNextChunk();
          },
          error: (err) => {
            observer.error(err);
          },
        });
      }
    };

    // Start initial batch
    for (
      let i = 0;
      i < Math.min(this.MAX_CONCURRENT_CHUNKS, uploadTasks.length);
      i++
    ) {
      processNextChunk();
    }
  }

  /**
   * Upload a single chunk with retry logic
   */
  private uploadSingleChunk(
    chunk: Blob,
    fileName: string,
    path: string,
    chunkNumber: number,
    totalChunks: number,
    resourceType: string = 'auto',
  ): Observable<ChunkUploadProgress | string> {
    return new Observable((observer) => {
      const formData = new FormData();
      formData.append('file', chunk);

      // ALWAYS append the upload preset - it's required even for unsigned uploads
      // Cloudinary requires: upload_preset parameter for authentication/configuration
      formData.append('upload_preset', this.cloudinaryUploadPreset);

      formData.append('folder', `${path}/chunks`); // Store chunks in subfolder
      // Don't append resource_type since the endpoint path specifies it

      // Get the correct URL based on resource type
      const uploadUrl = this.getCloudinaryUploadUrl(resourceType);

      const xhr = new XMLHttpRequest();
      let retries = 0;

      const attemptUpload = () => {
        console.log(
          `Uploading chunk ${chunkNumber}/${totalChunks} to ${uploadUrl} (attempt ${retries + 1})`,
        );
        xhr.upload.addEventListener('progress', (event) => {
          if (event.lengthComputable) {
            const chunkProgress = Math.round(
              (event.loaded / event.total) * 100,
            );
            const totalProgress = Math.round(
              ((chunkNumber - 1) * 100 + chunkProgress) / totalChunks,
            );

            observer.next({
              totalProgress,
              currentChunk: chunkNumber,
              totalChunks,
              chunkProgress,
              uploadedBytes: (chunkNumber - 1) * this.CHUNK_SIZE + event.loaded,
              totalBytes: totalChunks * this.CHUNK_SIZE,
              estimatedTimeRemaining: this.estimateTimeRemaining(
                (chunkNumber - 1) * this.CHUNK_SIZE + event.loaded,
                totalChunks * this.CHUNK_SIZE,
              ),
            });
          }
        });

        xhr.addEventListener('load', () => {
          if (xhr.status === 200) {
            const response = JSON.parse(
              xhr.responseText,
            ) as CloudinaryChunkResponse;
            console.log(
              `Chunk ${chunkNumber}/${totalChunks} uploaded:`,
              response.secure_url,
            );
            observer.next(response.secure_url);
            observer.complete();
          } else if (xhr.status >= 500 && retries < this.RETRY_ATTEMPTS) {
            // Server error - retry
            console.warn(
              `Chunk ${chunkNumber} got ${xhr.status}, retrying (${retries + 1}/${this.RETRY_ATTEMPTS})`,
            );
            retries++;
            setTimeout(attemptUpload, this.RETRY_DELAY * retries); // Exponential backoff
          } else {
            observer.error(
              new Error(
                `Chunk upload failed (${xhr.status}): ${xhr.responseText}`,
              ),
            );
          }
        });

        xhr.addEventListener('error', () => {
          if (retries < this.RETRY_ATTEMPTS) {
            console.warn(
              `Chunk ${chunkNumber} error, retrying (${retries + 1}/${this.RETRY_ATTEMPTS})`,
            );
            retries++;
            setTimeout(attemptUpload, this.RETRY_DELAY * retries);
          } else {
            observer.error(
              new Error(
                `Chunk ${chunkNumber} upload failed after ${this.RETRY_ATTEMPTS} retries`,
              ),
            );
          }
        });

        xhr.addEventListener('abort', () => {
          observer.error(new Error(`Chunk ${chunkNumber} upload aborted`));
        });

        xhr.open('POST', uploadUrl);
        xhr.send(formData);
      };

      attemptUpload();
    });
  }

  /**
   * Emit progress update for multi-chunk uploads
   */
  private emitProgress(
    observer: any,
    completedChunks: number,
    totalChunks: number,
    currentChunk: number,
  ): void {
    const totalProgress = Math.round((completedChunks / totalChunks) * 100);

    observer.next({
      totalProgress,
      currentChunk,
      totalChunks,
      chunkProgress: 100,
      uploadedBytes: completedChunks * this.CHUNK_SIZE,
      totalBytes: totalChunks * this.CHUNK_SIZE,
    });

    console.log(
      `⏳ Progress: ${completedChunks}/${totalChunks} chunks (${totalProgress}%)`,
    );
  }

  /**
   * Estimate time remaining based on upload speed
   */
  private estimateTimeRemaining(
    uploadedBytes: number,
    totalBytes: number,
  ): number {
    if (uploadedBytes === 0 || this.uploadStartTime === 0) return 0;

    const elapsedMs = Date.now() - this.uploadStartTime;
    const uploadSpeed = uploadedBytes / (elapsedMs / 1000); // bytes per second
    const remainingBytes = totalBytes - uploadedBytes;

    return Math.round(remainingBytes / uploadSpeed);
  }

  /**
   * Get human-readable time string
   */
  getTimeDisplay(seconds: number): string {
    if (seconds < 60) return `${Math.round(seconds)}s`;
    const minutes = Math.floor(seconds / 60);
    const secs = Math.round(seconds % 60);
    return `${minutes}m ${secs}s`;
  }

  /**
   * Calculate whether file should use chunked upload
   * Use chunking for files > 10MB or when network is slow
   */
  shouldUseChunkedUpload(fileSize: number): boolean {
    return fileSize > 10 * 1024 * 1024; // > 10 MB
  }

  /**
   * Get optimal chunk size based on network speed (future enhancement)
   */
  getOptimalChunkSize(bandwidth?: number): number {
    // Default to 5MB
    if (!bandwidth) return this.CHUNK_SIZE;

    // Adjust based on bandwidth if provided (bytes per second)
    // Keep chunk upload time around 5-10 seconds
    const targetChunkTime = 7; // seconds
    const optimalSize = bandwidth * targetChunkTime;

    // Clamp between 1MB and 50MB
    return Math.max(1024 * 1024, Math.min(50 * 1024 * 1024, optimalSize));
  }
}
