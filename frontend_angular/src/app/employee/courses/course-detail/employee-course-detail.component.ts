import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatRadioModule } from '@angular/material/radio';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { FormsModule } from '@angular/forms';
import { CourseService } from '../../../core/services/course.service';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { EnrollmentService } from '../../../core/services/enrollment.service';
import { SnackBarService } from '../../../core/services/snackbar.service';
import {
  Course,
  Section,
  Enrollment,
  QcmAttemptResult,
  AttemptInfo,
} from '../../../models';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { SkeletonComponent } from '../../../shared/components/skeleton/skeleton.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-employee-course-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatExpansionModule,
    MatRadioModule,
    MatProgressBarModule,
    StatusBadgeComponent,
    PageHeaderComponent,
    SkeletonComponent,
    EmptyStateComponent,
  ],
  templateUrl: './employee-course-detail.component.html',
})
export class EmployeeCourseDetailComponent implements OnInit {
  course: Course | null = null;
  sections: Section[] = [];
  enrollment: Enrollment | null = null;
  selectedAnswers: Record<string, number> = {};
  lastResult: QcmAttemptResult | null = null;
  lastResultSectionId: number | null = null;
  submitting = false;
  attemptCounts: Record<number, AttemptInfo> = {};

  constructor(
    private route: ActivatedRoute,
    private courseService: CourseService,
    private enrollmentService: EnrollmentService,
    private notify: SnackBarService,
    private sanitizer: DomSanitizer,
  ) {}

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
    const courseId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadCourse(courseId);
    this.loadSections(courseId);
    this.loadEnrollment();
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
        this.sections = sortedSections;

        // Load questions for QCM sections (not included in sections API response)
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

            // Load attempt count for this QCM section
            this.courseService.getMyAttemptCount(section.id).subscribe({
              next: (info) => {
                this.attemptCounts[section.id] = info;
              },
              error: () => {
                // Default to no limit if fetch fails
                this.attemptCounts[section.id] = {
                  attemptsUsed: 0,
                  maxAttempts: null,
                  exhausted: false,
                };
              },
            });
          }
        });
      },
    });
  }

  loadEnrollment(): void {
    this.enrollmentService.getMyEnrollments().subscribe({
      next: (enrollments) => {
        const courseId = Number(this.route.snapshot.paramMap.get('id'));
        this.enrollment =
          enrollments.find((e) => e.courseId === courseId) || null;
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

  getStatusClass(status: string): string {
    const classes: Record<string, string> = {
      PASSED: 'bg-green-100 text-green-800',
      FAILED: 'bg-red-100 text-red-800',
      IN_PROGRESS: 'bg-blue-100 text-blue-800',
      OVERDUE: 'bg-orange-100 text-orange-800',
    };
    return classes[status] || 'bg-gray-100 text-gray-800';
  }

  getAnswerClass(
    sectionId: number,
    questionId: number,
    optionIndex: number,
  ): string {
    if (!this.lastResult || this.lastResultSectionId !== sectionId) return '';
    const answerResult = this.lastResult.answers?.find(
      (a) => a.questionId === questionId,
    );
    if (!answerResult) return '';
    if (optionIndex === answerResult.correctAnswerIndex)
      return 'text-green-700 font-bold';
    if (
      optionIndex === answerResult.selectedAnswerIndex &&
      !answerResult.correct
    )
      return 'text-red-500';
    return '';
  }

  isExhausted(section: Section): boolean {
    const info = this.attemptCounts[section.id];
    return info?.exhausted ?? false;
  }

  hasUsedAttempts(section: Section): boolean {
    const info = this.attemptCounts[section.id];
    return info != null && info.attemptsUsed > 0;
  }

  attemptsLabel(section: Section): string | null {
    const info = this.attemptCounts[section.id];
    if (!info || info.maxAttempts == null) return null;
    return `${info.attemptsUsed} / ${info.maxAttempts} attempts used`;
  }

  private buildAttempt(section: Section) {
    const answers = (section.questions || []).map((q) => ({
      questionId: q.id,
      selectedAnswerIndex: this.selectedAnswers[section.id + '_' + q.id] ?? -1,
    }));
    return { sectionId: section.id, answers };
  }

  private validateAllQuestionsAnswered(section: Section): boolean {
    const unanswered = (section.questions || []).filter(
      (q) => !this.selectedAnswers.hasOwnProperty(section.id + '_' + q.id),
    );
    if (unanswered.length > 0) {
      this.notify.error(
        `Please answer all ${unanswered.length} unanswered question(s) before submitting.`,
      );
      return false;
    }
    return true;
  }

  submitPractice(section: Section): void {
    if (!this.enrollment) return;
    if (!this.validateAllQuestionsAnswered(section)) return;
    this.submitting = true;
    const attempt = this.buildAttempt(section);
    this.enrollmentService
      .submitPracticeAttempt(this.enrollment.id, attempt)
      .subscribe({
        next: (result) => {
          this.lastResult = result;
          this.lastResultSectionId = section.id;
          this.submitting = false;
          // Update attempt count from response
          if (result.attemptsUsed !== undefined) {
            this.attemptCounts[section.id] = {
              attemptsUsed: result.attemptsUsed,
              maxAttempts: result.maxAttempts ?? null,
              exhausted: result.exhausted ?? false,
            };
          }
          this.notify.success(
            `Practice: ${result.correctAnswers}/${result.totalQuestions} correct`,
          );
        },
        error: (err) => {
          this.submitting = false;
          const msg = err?.error?.message || 'Practice attempt failed';
          this.notify.error(msg);
        },
      });
  }

  submitFinal(section: Section): void {
    if (!this.enrollment) return;
    if (!this.validateAllQuestionsAnswered(section)) return;
    this.submitting = true;
    const attempt = this.buildAttempt(section);
    this.enrollmentService
      .submitFinalAttempt(this.enrollment.id, attempt)
      .subscribe({
        next: (result) => {
          this.lastResult = result;
          this.lastResultSectionId = section.id;
          this.submitting = false;
          // Update attempt count from response
          if (result.attemptsUsed !== undefined) {
            this.attemptCounts[section.id] = {
              attemptsUsed: result.attemptsUsed,
              maxAttempts: result.maxAttempts ?? null,
              exhausted: result.exhausted ?? false,
            };
          }
          this.loadEnrollment(); // Reload to get updated status/score
          const msg = result.passed
            ? `🎉 Congratulations! You passed with ${result.score}%!`
            : `Score: ${result.score}%. You need ${this.course?.passingScore}% to pass.`;
          if (result.passed) {
            this.notify.success(msg);
          } else {
            this.notify.error(msg);
          }
        },
        error: (err) => {
          this.submitting = false;
          const msg = err?.error?.message || 'Final attempt failed';
          this.notify.error(msg);
        },
      });
  }
}
