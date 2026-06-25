export interface Course {
  id: number;
  title: string;
  description: string;
  status: 'DRAFT' | 'PUBLISHED';
  passingScore: number;
  createdBy?: number;
  createdByName?: string;
  sections?: Section[];
  assignments?: CourseAssignment[];
  createdAt?: string;
  updatedAt?: string;
}

export interface CourseCreateRequest {
  title: string;
  description: string;
  status: 'DRAFT' | 'PUBLISHED';
  passingScore: number;
}

export interface Section {
  id: number;
  courseId: number;
  title: string;
  type: 'VIDEO' | 'IMAGE' | 'AUDIO' | 'PDF' | 'TEXT' | 'QCM';
  content?: string;
  mediaUrl?: string;
  fileDescription?: string;
  orderIndex: number;
  qcmType?: 'PRACTICE' | 'FINAL';
  maxAttempts?: number;
  llmDraftEnabled?: boolean;
  questions?: Question[];
  createdAt?: string;
  updatedAt?: string;
}

export interface SectionCreateRequest {
  title: string;
  contentType: 'VIDEO' | 'IMAGE' | 'AUDIO' | 'PDF' | 'TEXT' | 'QCM';
  content?: string;
  contentUrl?: string;
  fileDescription?: string;
  orderIndex: number;
  qcmType?: 'PRACTICE' | 'FINAL';
  llmDraftEnabled?: boolean;
}

export interface Question {
  id: number;
  sectionId: number;
  text: string;
  options: string[];
  correctAnswerIndex: number;
  createdAt?: string;
}

export interface QuestionCreateRequest {
  text: string;
  options: string[];
  correctAnswerIndex: number;
}

export interface QCMQuestionRequest {
  questionText: string;
  optionA: string;
  optionB: string;
  optionC: string;
  optionD: string;
  correctOption: string;
}

export interface CourseAssignment {
  id: number;
  courseId: number;
  courseTitle?: string;
  departmentId: number;
  departmentName?: string;
  deadlineDate: string;
  createdAt?: string;
}

export interface CourseAssignmentRequest {
  departmentId: number;
  deadlineDate: string | null;
}

export interface GenerateQcmRequest {
  topic?: string;
  numberOfQuestions?: number;
}
