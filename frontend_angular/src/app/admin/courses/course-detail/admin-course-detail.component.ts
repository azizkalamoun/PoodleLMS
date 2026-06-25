import {
  Component,
  OnInit,
  NgZone,
  TemplateRef,
  ViewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
  FormArray,
} from '@angular/forms';
import { Observable } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatTabsModule } from '@angular/material/tabs';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { CourseService } from '../../../core/services/course.service';
import { DepartmentService } from '../../../core/services/department.service';
import { SnackBarService } from '../../../core/services/snackbar.service';
import {
  CloudinaryUploadService,
  ChunkedUploadService,
  CloudinaryAssetService,
} from '../../../core/services';
import { ModalConfig } from '../../../core/utils/modal-config';
import {
  Course,
  Section,
  Question,
  Department,
  CourseAssignment,
} from '../../../models';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { FormDialogComponent } from '../../../shared/components/form-dialog/form-dialog.component';
import { SkeletonComponent } from '../../../shared/components/skeleton/skeleton.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-admin-course-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatExpansionModule,
    MatTabsModule,
    MatDialogModule,
    MatProgressBarModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatTooltipModule,
    DragDropModule,
    SkeletonComponent,
    StatusBadgeComponent,
    PageHeaderComponent,
    EmptyStateComponent,
  ],
  templateUrl: './admin-course-detail.component.html',
})
export class AdminCourseDetailComponent implements OnInit {
  course: Course | null = null;
  sections: Section[] = [];
  assignments: CourseAssignment[] = [];
  departments: Department[] = [];

  sectionForm!: FormGroup;
  questionForm!: FormGroup;
  assignForm!: FormGroup;

  editingSectionId: number | null = null;
  editingQuestionId: number | null = null;
  editingQuestionSectionId: number | null = null;

  selectedFile: File | null = null;
  uploading = false;
  uploadProgress = 0;
  oldMediaUrl: string | undefined | null = null;
  dialogRef: any = null;

  @ViewChild('sectionFormTemplate') sectionFormTemplate!: TemplateRef<any>;
  @ViewChild('questionFormTemplate') questionFormTemplate!: TemplateRef<any>;
  @ViewChild('assignFormTemplate') assignFormTemplate!: TemplateRef<any>;

  constructor(
    private route: ActivatedRoute,
    private courseService: CourseService,
    private departmentService: DepartmentService,
    private uploadService: CloudinaryUploadService,
    private chunkedUploadService: ChunkedUploadService,
    private assetService: CloudinaryAssetService,
    private fb: FormBuilder,
    private dialog: MatDialog,
    private notify: SnackBarService,
    private ngZone: NgZone,
    private sanitizer: DomSanitizer,
  ) {
    this.initForms();
  }

  /**
   * Convert PDF URL to viewer URL
   * Handles multiple PDF sources: Cloudinary, direct URLs, and base64
   */
  getPdfViewerUrl(mediaUrl: string): SafeResourceUrl {
    if (!mediaUrl) return this.sanitizer.bypassSecurityTrustResourceUrl('');

    // If already a data URL, return as-is
    if (mediaUrl.startsWith('data:')) {
      return this.sanitizer.bypassSecurityTrustResourceUrl(mediaUrl);
    }

    // If it's a Cloudinary URL, add format parameter for better PDF handling
    if (mediaUrl.includes('cloudinary.com')) {
      // For Cloudinary PDFs, we can use the URL directly
      // but ensure proper content-type handling
      return this.sanitizer.bypassSecurityTrustResourceUrl(mediaUrl);
    }

    // For other URLs, use Google Docs viewer as fallback
    // This works for most publicly accessible PDFs
    if (mediaUrl.startsWith('http://') || mediaUrl.startsWith('https://')) {
      const viewerUrl = `https://docs.google.com/gview?url=${encodeURIComponent(mediaUrl)}&embedded=true`;
      return this.sanitizer.bypassSecurityTrustResourceUrl(viewerUrl);
    }

    // If it's a relative path, prepend the backend base URL
    // Adjust this based on your backend configuration
    const baseUrl = window.location.origin;
    const fullUrl = mediaUrl.startsWith('/')
      ? `${baseUrl}${mediaUrl}`
      : `${baseUrl}/${mediaUrl}`;
    const viewerUrl = `https://docs.google.com/gview?url=${encodeURIComponent(fullUrl)}&embedded=true`;
    return this.sanitizer.bypassSecurityTrustResourceUrl(viewerUrl);
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadCourse(id);
    this.loadSections(id);
    this.loadAssignments(id);
    this.loadDepartments();
  }

  private initForms(): void {
    this.sectionForm = this.fb.group({
      title: ['', Validators.required],
      type: ['TEXT', Validators.required],
      content: [''],
      fileDescription: [''],
      mediaUrl: [''],
      orderIndex: [0, Validators.required],
      qcmType: ['PRACTICE'],
      maxAttempts: [3, [Validators.min(1), Validators.max(20)]],
      llmDraftEnabled: [true],
    });

    this.questionForm = this.fb.group({
      text: ['', Validators.required],
      options: this.fb.array([
        this.fb.control('', Validators.required),
        this.fb.control('', Validators.required),
        this.fb.control('', Validators.required),
        this.fb.control('', Validators.required),
      ]),
      correctAnswerIndex: [
        0,
        [Validators.required, Validators.min(0), Validators.max(3)],
      ],
    });

    this.assignForm = this.fb.group({
      departmentId: [null, Validators.required],
      deadline: [null, Validators.required],
    });
  }

  // Check if section form can be submitted based on type
  canSubmitSection(): boolean {
    if (!this.sectionForm) return false;
    if (this.sectionForm.invalid) return false;

    const type = this.sectionForm.get('type')?.value;
    const mediaTypes = ['VIDEO', 'AUDIO', 'PDF', 'IMAGE'];

    // For TEXT type, only need title
    if (type === 'TEXT') {
      return !!this.sectionForm.get('content')?.value;
    }

    // For QCM type, no file upload needed (questions can be added after creation)
    if (type === 'QCM') {
      return true;
    }

    // For media types, need mediaUrl
    if (mediaTypes.includes(type)) {
      return !!this.sectionForm.get('mediaUrl')?.value;
    }

    return false;
  }

  get optionsArray(): FormArray {
    return this.questionForm.get('options') as FormArray;
  }

  addOption(): void {
    this.optionsArray.push(this.fb.control('', Validators.required));
  }

  removeOption(index: number): void {
    this.optionsArray.removeAt(index);
  }

  isEditingNumericSection(): boolean {
    return (
      typeof this.editingSectionId === 'number' &&
      this.editingSectionId !== null
    );
  }

  loadCourse(id: number): void {
    this.courseService.getById(id).subscribe({
      next: (course) => (this.course = course),
      error: () => this.notify.error('Failed to load course'),
    });
  }

  loadSections(courseId: number): void {
    this.courseService.getSections(courseId).subscribe({
      next: (sections) => {
        const sortedSections = sections.sort(
          (a, b) => a.orderIndex - b.orderIndex,
        );

        // Load questions for QCM sections
        sortedSections.forEach((section) => {
          if (section.type === 'QCM' && section.id) {
            this.courseService.getQuestions(courseId, section.id).subscribe({
              next: (questions) => {
                section.questions = questions;
              },
              error: (err) => {
                console.error(
                  `Failed to load questions for section ${section.id}:`,
                  err,
                );
                section.questions = [];
              },
            });
          }
        });

        this.sections = sortedSections;
      },
      error: () => console.error('Failed to load sections'),
    });
  }

  loadAssignments(courseId: number): void {
    this.courseService.getAssignments(courseId).subscribe({
      next: (assignments) => (this.assignments = assignments),
      error: () => console.error('Failed to load assignments'),
    });
  }

  loadDepartments(): void {
    this.departmentService.getAll().subscribe({
      next: (depts) => {
        this.departments = depts;
        console.log('Departments loaded:', depts);
      },
      error: (err) => {
        console.error('Failed to load departments:', err);
        this.notify.error('Failed to load departments');
      },
    });
  }

  getSectionIcon(type: string): string {
    const icons: Record<string, string> = {
      TEXT: 'article',
      VIDEO: 'videocam',
      IMAGE: 'image',
      AUDIO: 'audiotrack',
      PDF: 'picture_as_pdf',
      QCM: 'quiz',
    };
    return icons[type] || 'description';
  }

  getAcceptType(): string {
    const type = this.sectionForm.get('type')?.value;
    const accepts: Record<string, string> = {
      VIDEO: 'video/*',
      IMAGE: 'image/*',
      AUDIO: 'audio/*',
      PDF: '.pdf',
    };
    return accepts[type] || '*/*';
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.selectedFile = input.files[0];
      // Update form's mediaUrl field so validation passes
      this.sectionForm.patchValue({
        mediaUrl: this.selectedFile.name,
      });
    }
  }

  onSectionSubmit(): void {
    if (this.sectionForm.invalid || !this.course) return;

    const data = this.sectionForm.value;

    // All media types that require upload
    const mediaTypes = ['VIDEO', 'AUDIO', 'PDF', 'IMAGE'];

    if (this.selectedFile && mediaTypes.includes(data.type)) {
      // Validate file size before upload
      const sizeError = this.uploadService.validateFileSize(this.selectedFile);
      if (sizeError) {
        this.notify.error(sizeError);
        return;
      }

      this.uploading = true;
      this.uploadProgress = 0;
      const path = this.uploadService.getUploadPath(
        this.course.id,
        data.type,
        this.selectedFile.name,
      );

      // If updating an existing section with old asset, delete old asset first
      if (
        this.editingSectionId &&
        this.oldMediaUrl &&
        this.oldMediaUrl !== data.mediaUrl
      ) {
        console.log('Deleting old asset before upload:', this.oldMediaUrl);
        this.assetService.deleteAssetIfCloudinary(this.oldMediaUrl).subscribe({
          next: () => {
            console.log('Old asset deleted, proceeding with new upload');
            this.performFileUpload(this.selectedFile!, path, data);
          },
          error: (err) => {
            console.warn(
              'Old asset deletion failed, continuing with new upload:',
              err,
            );
            this.performFileUpload(this.selectedFile!, path, data);
          },
        });
      } else {
        // New section or same file, proceed directly
        this.performFileUpload(this.selectedFile, path, data);
      }
    } else {
      this.submitSection(data);
    }
  }

  private performFileUpload(file: File, path: string, data: any): void {
    if (!this.course) return;

    console.log('Starting file upload:', {
      fileName: file.name,
      fileSize: file.size,
      fileSizeDisplay: this.uploadService.getFileSizeDisplay(file.size),
      type: data.type,
      path,
      useChunked: this.chunkedUploadService.shouldUseChunkedUpload(file.size),
    });

    // Choose between chunked or regular upload based on file size
    const uploadObservable$ = this.chunkedUploadService.shouldUseChunkedUpload(
      file.size,
    )
      ? this.chunkedUploadService.uploadFileChunked(file, path)
      : this.uploadService.uploadFileWithProgress(file, path);

    (uploadObservable$ as Observable<any>).subscribe({
      next: (result: string | any) => {
        console.log('Upload handler received:', result);

        // Check if it's a progress update or the final URL
        if (typeof result === 'string') {
          // Final URL received
          console.log('Upload complete, URL:', result);
          this.ngZone.run(() => {
            data.mediaUrl = result;
            this.submitSection(data);
            this.uploading = false;
            this.uploadProgress = 0;
          });
        } else if (result && typeof result === 'object') {
          // Progress update - use total progress for chunked, progress for regular
          const progress =
            (result as any).totalProgress || (result as any).progress || 0;
          console.log(
            `Progress: ${progress}%`,
            (result as any).totalChunks
              ? `(${(result as any).currentChunk}/${(result as any).totalChunks} chunks)`
              : '',
          );
          this.ngZone.run(() => {
            this.uploadProgress = progress;
          });
        } else if (result === 'completed') {
          // Multi-chunk upload completion signal
          console.log('Chunked upload complete');
          this.ngZone.run(() => {
            this.uploading = false;
            this.uploadProgress = 100;
            // Wait for final URL which comes as string after this
          });
        }
      },
      error: (err: any) => {
        console.error('Upload error:', err);
        this.ngZone.run(() => {
          this.uploading = false;
          this.uploadProgress = 0;

          // Extract error message from response
          let errorMessage = 'Upload failed. Please try again.';
          if (err?.message) {
            errorMessage = err.message;
          } else if (err?.error?.error?.message) {
            errorMessage = err.error.error.message;
          }

          this.notify.error(errorMessage);
        });
      },
    });
  }

  private submitSection(data: any): void {
    if (!this.course) return;

    // Ensure contentType is provided
    if (!data.type) {
      this.notify.error('Content type is required');
      return;
    }

    // Determine contentUrl based on type
    let contentUrl: string | null = null;
    if (data.type === 'TEXT') {
      contentUrl = data.content || null;
    } else if (data.type === 'QCM') {
      // QCM sections don't need a contentUrl
      contentUrl = null;
    } else {
      // Media types (VIDEO, AUDIO, PDF, IMAGE)
      contentUrl = data.mediaUrl || null;
    }

    // Build clean payload - only include properties that are needed
    const payload: any = {
      title: data.title,
      contentType: data.type,
      orderIndex: data.orderIndex || 0,
    };

    // Add optional fileDescription only if provided
    if (data.fileDescription) {
      payload.fileDescription = data.fileDescription;
    }

    // Add contentUrl only if it has a value
    if (contentUrl) {
      payload.contentUrl = contentUrl;
    }

    // Add QCM-specific fields if applicable
    if (data.type === 'QCM') {
      payload.qcmType = data.qcmType || 'PRACTICE'; // Default to PRACTICE if not specified
      payload.maxAttempts = data.maxAttempts ?? 3;
      payload.llmDraftEnabled = data.llmDraftEnabled || false;
    }

    console.log('Section payload:', payload);
    console.log('Course ID:', this.course.id);
    console.log('Editing Section ID:', this.editingSectionId);

    if (this.editingSectionId) {
      // Update existing section
      this.courseService
        .updateSection(this.course.id, this.editingSectionId, payload)
        .subscribe({
          next: () => {
            this.notify.success('Section updated!');
            this.loadSections(this.course!.id);
            if (this.dialogRef) {
              this.dialogRef.close(true);
            }
          },
          error: (err) => {
            console.error('Update section error:', err);
            console.error('Error details:', err.error);
            this.notify.error(
              'Failed to update section: ' +
                (err.error?.message || 'Unknown error'),
            );
          },
        });
    } else {
      // Create new section
      console.log('Creating new section with payload:', payload);
      this.courseService.createSection(this.course.id, payload).subscribe({
        next: (response) => {
          console.log('Section created successfully:', response);
          this.notify.success('Section created!');
          this.loadSections(this.course!.id);
          if (this.dialogRef) {
            this.dialogRef.close(true);
          }
        },
        error: (err) => {
          console.error('Create section error:', err);
          console.error('Error status:', err.status);
          console.error('Error body:', err.error);
          this.notify.error(
            'Failed to create section: ' +
              (err.error?.message || 'Unknown error'),
          );
        },
      });
    }
  }

  editSection(section: Section): void {
    this.editingSectionId = section.id;
    this.oldMediaUrl = section.mediaUrl;
    this.sectionForm.patchValue({
      title: section.title,
      type: section.type,
      content: section.content,
      fileDescription: section.fileDescription,
      mediaUrl: section.mediaUrl,
      orderIndex: section.orderIndex,
      qcmType: section.qcmType || 'PRACTICE',
      maxAttempts: section.maxAttempts ?? 3,
      llmDraftEnabled: section.llmDraftEnabled || false,
    });

    this.dialogRef = this.dialog.open(
      this.sectionFormTemplate,
      ModalConfig.getLargeDialogConfig(),
    );

    this.dialogRef.afterClosed().subscribe((result: any) => {
      this.dialogRef = null;
      if (result !== false) {
        this.resetSectionForm();
      }
    });
  }

  resetSectionForm(): void {
    this.editingSectionId = null;
    this.selectedFile = null;
    this.oldMediaUrl = null;
    this.initForms();
  }

  openAddSectionModal(): void {
    this.editingSectionId = null;
    this.selectedFile = null;
    this.oldMediaUrl = null;
    this.sectionForm.reset({ type: 'TEXT', orderIndex: 0 });

    this.dialogRef = this.dialog.open(
      this.sectionFormTemplate,
      ModalConfig.getLargeDialogConfig(),
    );

    this.dialogRef.afterClosed().subscribe((result: any) => {
      this.dialogRef = null;
      if (result !== false) {
        this.resetSectionForm();
      }
    });
  }

  deleteSection(section: Section): void {
    if (!this.course) return;
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Delete Section', message: `Delete "${section.title}"?` },
    });
    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed && this.course) {
        // Delete associated Cloudinary asset if section has media
        if (section.mediaUrl && section.mediaUrl.includes('cloudinary.com')) {
          console.log('Cleaning up associated asset:', section.mediaUrl);
          this.assetService
            .deleteAssetIfCloudinary(section.mediaUrl)
            .subscribe({
              next: () => {
                console.log('Asset cleanup complete');
                this.performSectionDeletion(this.course!.id, section.id);
              },
              error: (err) => {
                console.warn(
                  'Asset cleanup encountered error, proceeding with section deletion:',
                  err,
                );
                this.performSectionDeletion(this.course!.id, section.id);
              },
            });
        } else {
          // No media asset to delete, proceed directly
          this.performSectionDeletion(this.course.id, section.id);
        }
      }
    });
  }

  private performSectionDeletion(courseId: number, sectionId: number): void {
    this.courseService.deleteSection(courseId, sectionId).subscribe({
      next: () => {
        this.notify.success('Section deleted!');
        this.loadSections(courseId);
      },
      error: () => this.notify.error('Delete failed'),
    });
  }

  editQuestion(sectionId: number, question: Question): void {
    if (!this.course) return;

    // Set up the form with the question data
    this.questionForm.patchValue({
      text: question.text,
      correctAnswerIndex: question.correctAnswerIndex,
    });

    // Clear and repopulate options array
    const optionsControl = this.questionForm.get('options') as FormArray;
    while (optionsControl.length > 0) {
      optionsControl.removeAt(0);
    }

    question.options.forEach((opt) => {
      optionsControl.push(this.fb.control(opt, Validators.required));
    });

    // Set editing state
    this.editingQuestionId = question.id;
    this.editingQuestionSectionId = sectionId;

    // Open dialog
    this.dialogRef = this.dialog.open(
      this.questionFormTemplate,
      ModalConfig.getLargeDialogConfig(),
    );

    this.dialogRef.afterClosed().subscribe((result: any) => {
      this.dialogRef = null;
      if (result !== false) {
        this.cancelEditQuestion();
      }
    });
  }

  cancelEditQuestion(): void {
    this.editingQuestionId = null;
    this.editingQuestionSectionId = null;
    this.initForms();
  }

  openAddQuestionModal(sectionId: number): void {
    this.editingQuestionId = null;
    this.editingQuestionSectionId = sectionId;
    this.questionForm.reset({
      text: '',
      options: ['', '', '', ''],
      correctAnswerIndex: 0,
    });

    this.dialogRef = this.dialog.open(
      this.questionFormTemplate,
      ModalConfig.getLargeDialogConfig(),
    );

    this.dialogRef.afterClosed().subscribe((result: any) => {
      this.dialogRef = null;
      if (result !== false) {
        this.cancelEditQuestion();
      }
    });
  }

  onQuestionSubmit(sectionId: number | string | null): void {
    if (this.questionForm.invalid || !this.course) return;

    // Ensure sectionId is a valid number (not 'new-temp' string for new sections)
    const numericSectionId = typeof sectionId === 'number' ? sectionId : null;
    if (!numericSectionId || numericSectionId === 0) {
      this.notify.warning(
        'Please save the section first before adding questions',
      );
      return;
    }

    const data = this.questionForm.value;

    // Validate that we have exactly 4 options
    if (!data.options || data.options.length < 4) {
      this.notify.error('You must provide exactly 4 options for a question');
      console.error('Invalid options count:', data.options?.length);
      return;
    }

    // Validate correctAnswerIndex is within range [0-3]
    if (data.correctAnswerIndex < 0 || data.correctAnswerIndex > 3) {
      this.notify.error('Correct answer index must be between 0 and 3');
      console.error('Invalid correctAnswerIndex:', data.correctAnswerIndex);
      return;
    }

    // Transform form data to backend format
    // Frontend sends: { text, options: ['A', 'B', 'C', 'D'], correctAnswerIndex: 0 }
    // Backend expects: { questionText, optionA, optionB, optionC, optionD, correctOption: 'A' }
    const transformedData = {
      questionText: data.text,
      optionA: data.options[0],
      optionB: data.options[1],
      optionC: data.options[2],
      optionD: data.options[3],
      correctOption: String.fromCharCode(65 + data.correctAnswerIndex), // 0 -> 'A', 1 -> 'B', etc.
    };

    console.log('Submitting question with data:', transformedData);

    // If editing, update; otherwise, create
    const request$ = this.editingQuestionId
      ? this.courseService.updateQuestion(
          this.course.id,
          numericSectionId,
          this.editingQuestionId,
          transformedData,
        )
      : this.courseService.createQuestion(
          this.course.id,
          numericSectionId,
          transformedData,
        );

    request$.subscribe({
      next: () => {
        const message = this.editingQuestionId
          ? 'Question updated!'
          : 'Question added!';
        this.notify.success(message);
        this.editingQuestionId = null;
        this.editingQuestionSectionId = null;
        this.loadSections(this.course!.id);
        this.initForms();
        if (this.dialogRef) {
          this.dialogRef.close(true);
        }
      },
      error: (err) => {
        console.error('Failed to save question:', err);
        const errorMessage =
          err?.error?.message ||
          err?.error?.detail ||
          'Failed to save question';
        this.notify.error(`Error: ${errorMessage}`);
      },
    });
  }

  deleteQuestion(sectionId: number, questionId: number): void {
    if (!this.course) return;
    this.courseService
      .deleteQuestion(this.course.id, sectionId, questionId)
      .subscribe({
        next: () => {
          this.notify.success('Question deleted!');
          this.loadSections(this.course!.id);
        },
        error: () => this.notify.error('Delete failed'),
      });
  }

  generateQcmDraft(section: Section): void {
    if (!this.course) return;

    console.log('🤖 Generating QCM draft for section:', section.id);
    this.courseService
      .generateQcmDraft(this.course.id, section.id, { numberOfQuestions: 5 })
      .subscribe({
        next: (generatedQuestions) => {
          console.log('QCM draft generated successfully:', generatedQuestions);
          // Update the section's questions
          const updatedSections = this.sections.map((s) =>
            s.id === section.id ? { ...s, questions: generatedQuestions } : s,
          );
          this.sections = updatedSections;
          this.notify.success('QCM draft generated successfully!');
        },
        error: (error) => {
          console.error('Error generating QCM draft:', error);
          this.notify.error(
            'Failed to generate draft: ' +
              (error.error?.message || error.message || 'Unknown error'),
          );
        },
      });
  }

  onSectionDrop(event: any): void {
    if (!this.course) return;
    const { previousIndex, currentIndex } = event;
    if (previousIndex === currentIndex) return;

    // Update local array for immediate UI feedback
    const [movedSection] = this.sections.splice(previousIndex, 1);
    this.sections.splice(currentIndex, 0, movedSection);

    // Update orderIndex for all sections based on their new position
    this.sections.forEach((section, index) => {
      section.orderIndex = index;
    });

    // Send updates to backend - update each section with new orderIndex
    this.sections.forEach((section) => {
      // Prepare complete section data for backend (backend expects full SectionRequest)
      const updatePayload = {
        title: section.title,
        contentType: section.type, // Frontend 'type' maps to backend 'contentType'
        contentUrl: section.mediaUrl || null, // Frontend 'mediaUrl' maps to backend 'contentUrl'
        orderIndex: section.orderIndex,
      };

      this.courseService
        .updateSectionOrder(this.course!.id, section.id, updatePayload)
        .subscribe({
          next: () => {
            // Order updated successfully
            console.log(
              `Section ${section.id} order updated to ${section.orderIndex}`,
            );
          },
          error: (err) => {
            console.error('Failed to update section order:', err);
            this.notify.error('Failed to update section order');
            // Reload sections to restore correct order
            this.loadSections(this.course!.id);
          },
        });
    });
  }

  onAssignSubmit(): void {
    if (this.assignForm.invalid || !this.course) return;
    const formValue = this.assignForm.value;
    const data = {
      departmentId: formValue.departmentId,
      deadlineDate: formValue.deadline
        ? new Date(formValue.deadline).toISOString().split('T')[0]
        : null,
    };
    console.log('Assigning department with data:', data);
    this.courseService.assignToDepartment(this.course.id, data).subscribe({
      next: () => {
        this.notify.success('Department assigned!');
        this.loadAssignments(this.course!.id);
        this.assignForm.reset();
        if (this.dialogRef) {
          this.dialogRef.close(true);
        }
      },
      error: (err) => {
        console.error('Assignment failed:', err);
        this.notify.error(
          'Assignment failed: ' + (err.error?.message || 'Unknown error'),
        );
      },
    });
  }

  openAssignModal(): void {
    this.assignForm.reset();

    this.dialogRef = this.dialog.open(
      this.assignFormTemplate,
      ModalConfig.getMediumDialogConfig(),
    );

    this.dialogRef.afterClosed().subscribe((result: any) => {
      this.dialogRef = null;
      if (result !== false) {
        this.assignForm.reset();
      }
    });
  }

  removeAssignment(assignment: CourseAssignment): void {
    if (!this.course) return;
    this.courseService
      .removeAssignment(this.course.id, assignment.departmentId)
      .subscribe({
        next: () => {
          this.notify.success('Assignment removed!');
          this.loadAssignments(this.course!.id);
        },
        error: () => this.notify.error('Remove failed'),
      });
  }
}
