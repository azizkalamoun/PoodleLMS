package com.enterprise.poodle.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnalyticsResponse {

    @Data
    @Builder
    public static class CompletionRate {
        private Long courseId;
        private String courseTitle;
        private long totalAssigned;
        private long totalCompleted;
        private double completionRate;
    }

    @Data
    @Builder
    public static class PassRate {
        private Long courseId;
        private String courseTitle;
        private long totalAttempted;
        private long totalPassed;
        private double passRate;
    }

    @Data
    @Builder
    public static class DepartmentAverageScore {
        private Long departmentId;
        private String departmentName;
        private double averageScore;
        private long totalGraded;
    }

    @Data
    @Builder
    public static class OverdueEmployee {
        private Long employeeId;
        private String employeeName;
        private String email;
        private String departmentName;
        private String deadlineDate;
    }

    @Data
    @Builder
    public static class MostFailedQuestion {
        private Long questionId;
        private String questionText;
        private long totalAttempts;
        private long incorrectCount;
        private double failRate;
    }

    // Aggregate endpoints for dashboard

    @Data
    @Builder
    public static class OverviewData {
        private long totalEmployees;
        private long totalCourses;
        private long totalEnrollments;
        private double completionRate;
        private double passRate;
        private double averageScore;
    }

    @Data
    @Builder
    public static class CourseAnalyticsData {
        private Long courseId;
        private String courseTitle;
        private long enrollmentCount;
        private double completionRate;
        private double passRate;
        private double averageScore;
    }

    @Data
    @Builder
    public static class OverdueEnrollmentData {
        private Long enrollmentId;
        private String employeeName;
        private String courseTitle;
        private String deadline;
        private String departmentName;
    }

    @Data
    @Builder
    public static class FailedQuestionData {
        private Long questionId;
        private String questionText;
        private String courseTitle;
        private String sectionTitle;
        private double failureRate;
        private long totalAttempts;
    }

    @Data
    @Builder
    public static class DepartmentAnalyticsData {
        private Long departmentId;
        private String departmentName;
        private double completionRate;
        private double passRate;
        private double averageScore;
        private long employeeCount;
    }
}
