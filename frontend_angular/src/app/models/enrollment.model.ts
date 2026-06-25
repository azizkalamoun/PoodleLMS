export interface Enrollment {
  id: number;
  employeeId: number;
  employeeName?: string;
  courseId: number;
  courseTitle?: string;
  status: "IN_PROGRESS" | "PASSED" | "FAILED" | "OVERDUE";
  score?: number;
  attempts: number;
  deadline?: string;
  completedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface QcmAttempt {
  sectionId: number;
  answers: QcmAnswer[];
}

export interface QcmAnswer {
  questionId: number;
  selectedAnswerIndex: number;
}

export interface QcmAttemptResult {
  score: number;
  totalQuestions: number;
  correctAnswers: number;
  passed: boolean;
  answers: QcmAnswerResult[];
  attemptsUsed?: number;
  maxAttempts?: number | null;
  exhausted?: boolean;
}

export interface QcmAnswerResult {
  questionId: number;
  selectedAnswerIndex: number;
  correctAnswerIndex: number;
  correct: boolean;
}

export interface AttemptInfo {
  attemptsUsed: number;
  maxAttempts: number | null;
  exhausted: boolean;
}
