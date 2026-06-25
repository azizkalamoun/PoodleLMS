export interface AnalyticsOverview {
  totalEmployees: number;
  totalCourses: number;
  totalEnrollments: number;
  completionRate: number;
  passRate: number;
  averageScore: number;
}

export interface CourseAnalytics {
  courseId: number;
  courseTitle: string;
  enrollmentCount: number;
  completionRate: number;
  passRate: number;
  averageScore: number;
}

export interface OverdueEnrollment {
  enrollmentId: number;
  employeeName: string;
  courseTitle: string;
  deadline: string;
  departmentName: string;
}

export interface FailedQuestion {
  questionId: number;
  questionText: string;
  courseTitle: string;
  sectionTitle: string;
  failureRate: number;
  totalAttempts: number;
}

export interface DepartmentAnalytics {
  departmentId: number;
  departmentName: string;
  completionRate: number;
  passRate: number;
  averageScore: number;
  employeeCount: number;
}
