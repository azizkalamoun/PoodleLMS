import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { HttpParams } from "@angular/common/http";
import { map } from "rxjs/operators";
import { environment } from "../../../environments/environment";
import {
  Course,
  CourseCreateRequest,
  Section,
  SectionCreateRequest,
  Question,
  QuestionCreateRequest,
  QCMQuestionRequest,
  CourseAssignment,
  CourseAssignmentRequest,
  GenerateQcmRequest,
  AttemptInfo,
} from "../../models";

interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: "root",
})
export class CourseService {
  private apiUrl = `${environment.apiUrl}/courses`;

  constructor(private http: HttpClient) {}

  // Courses
  getAll(order?: "oldest" | "newest"): Observable<Course[]> {
    let params = new HttpParams();
    if (order) {
      params = params.set("order", order);
    }

    return this.http
      .get<PageResponse<Course>>(this.apiUrl, { params })
      .pipe(map((response) => response.content || []));
  }

  getById(id: number): Observable<Course> {
    return this.http.get<Course>(`${this.apiUrl}/${id}`);
  }

  create(course: CourseCreateRequest): Observable<Course> {
    return this.http.post<Course>(this.apiUrl, course);
  }

  update(id: number, course: Partial<Course>): Observable<Course> {
    return this.http.put<Course>(`${this.apiUrl}/${id}`, course);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Sections
  getSections(courseId: number): Observable<Section[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${courseId}/sections`).pipe(
      map((sections) =>
        sections.map((s) => ({
          id: s.id,
          courseId: s.courseId,
          title: s.title,
          type: s.contentType, // Map contentType → type
          content: s.contentUrl, // Map contentUrl → content
          mediaUrl: s.contentUrl, // Also map to mediaUrl for compatibility
          fileDescription: s.fileDescription, // Include fileDescription from API
          orderIndex: s.orderIndex,
          qcmType: s.qcmType, // PRACTICE or FINAL
          maxAttempts: s.maxAttempts,
          questions: s.questions || [],
          createdAt: s.createdAt,
          updatedAt: s.updatedAt,
        })),
      ),
    );
  }

  createSection(
    courseId: number,
    section: SectionCreateRequest,
  ): Observable<Section> {
    return this.http.post<Section>(
      `${this.apiUrl}/${courseId}/sections`,
      section,
    );
  }

  updateSection(
    courseId: number,
    sectionId: number,
    section: Partial<Section>,
  ): Observable<Section> {
    return this.http.put<Section>(
      `${environment.apiUrl}/sections/${sectionId}`,
      section,
    );
  }

  deleteSection(courseId: number, sectionId: number): Observable<void> {
    return this.http.delete<void>(
      `${environment.apiUrl}/sections/${sectionId}`,
    );
  }

  // Questions
  getQuestions(courseId: number, sectionId: number): Observable<Question[]> {
    return this.http
      .get<any[]>(`${environment.apiUrl}/sections/${sectionId}/questions`)
      .pipe(
        map((questions) =>
          questions.map((q) => ({
            id: q.id,
            sectionId: q.sectionId,
            // Map backend QCM format to frontend format
            text: q.questionText || q.text,
            options: [q.optionA, q.optionB, q.optionC, q.optionD].filter(
              (o) => o !== undefined,
            ),
            correctAnswerIndex: this.getCorrectAnswerIndex(q.correctOption),
            createdAt: q.createdAt,
          })),
        ),
      );
  }

  private getCorrectAnswerIndex(correctOption: string): number {
    if (!correctOption) return 0;
    // Convert 'A' -> 0, 'B' -> 1, 'C' -> 2, 'D' -> 3
    return correctOption.charCodeAt(0) - 65;
  }

  createQuestion(
    courseId: number,
    sectionId: number,
    question: QuestionCreateRequest | QCMQuestionRequest,
  ): Observable<Question> {
    return this.http.post<Question>(
      `${environment.apiUrl}/sections/${sectionId}/questions`,
      question,
    );
  }

  deleteQuestion(
    courseId: number,
    sectionId: number,
    questionId: number,
  ): Observable<void> {
    return this.http.delete<void>(
      `${environment.apiUrl}/sections/${sectionId}/questions/${questionId}`,
    );
  }

  updateQuestion(
    courseId: number,
    sectionId: number,
    questionId: number,
    question: QCMQuestionRequest,
  ): Observable<Question> {
    return this.http
      .put<any>(
        `${environment.apiUrl}/sections/${sectionId}/questions/${questionId}`,
        question,
      )
      .pipe(
        map((q) => ({
          id: q.id,
          sectionId: q.sectionId,
          text: q.questionText || q.text,
          options: [q.optionA, q.optionB, q.optionC, q.optionD].filter(
            (o) => o !== undefined,
          ),
          correctAnswerIndex: this.getCorrectAnswerIndex(q.correctOption),
          createdAt: q.createdAt,
        })),
      );
  }

  generateQcmDraft(
    courseId: number,
    sectionId: number,
    request: GenerateQcmRequest,
  ): Observable<Question[]> {
    return this.http.post<Question[]>(
      `${this.apiUrl}/${courseId}/sections/${sectionId}/generate-qcm`,
      request,
    );
  }

  // Assignments
  getAssignments(courseId: number): Observable<CourseAssignment[]> {
    return this.http.get<CourseAssignment[]>(
      `${this.apiUrl}/${courseId}/assigned-departments`,
    );
  }

  assignToDepartment(
    courseId: number,
    assignment: CourseAssignmentRequest,
  ): Observable<CourseAssignment> {
    const departmentId = assignment.departmentId;
    // Only send deadlineDate in request body
    const payload = { deadlineDate: assignment.deadlineDate };
    return this.http.post<CourseAssignment>(
      `${this.apiUrl}/${courseId}/assign/${departmentId}`,
      payload,
    );
  }

  removeAssignment(courseId: number, assignmentId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${courseId}/assign/${assignmentId}`,
    );
  }

  getMyAttemptCount(sectionId: number): Observable<AttemptInfo> {
    return this.http.get<AttemptInfo>(
      `${environment.apiUrl}/sections/${sectionId}/my-attempt-count`,
    );
  }

  updateSectionOrder(
    courseId: number,
    sectionId: number,
    payload: {
      title: string;
      contentType: string;
      contentUrl: string | null;
      orderIndex: number;
    },
  ): Observable<Section> {
    return this.http.put<Section>(
      `${environment.apiUrl}/sections/${sectionId}`,
      payload,
    );
  }
}
