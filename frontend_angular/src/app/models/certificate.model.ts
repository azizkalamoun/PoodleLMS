export interface Certificate {
  id: number;
  employeeId: number;
  employeeName?: string;
  courseId: number;
  courseTitle?: string;
  score: number;
  issuedAt: string;
  verificationCode: string;
  pdfUrl?: string;
  revoked?: boolean;
}
