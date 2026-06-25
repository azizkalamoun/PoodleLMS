package com.enterprise.poodle.controller;

import com.enterprise.poodle.dto.response.AnalyticsResponse;
import com.enterprise.poodle.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // Specific course analytics endpoints
    @GetMapping("/course/{courseId}/completion")
    public ResponseEntity<AnalyticsResponse.CompletionRate> getCourseCompletionRate(
            @PathVariable Long courseId) {
        return ResponseEntity.ok(analyticsService.getCourseCompletionRate(courseId));
    }

    @GetMapping("/course/{courseId}/pass-rate")
    public ResponseEntity<AnalyticsResponse.PassRate> getCoursePassRate(
            @PathVariable Long courseId) {
        return ResponseEntity.ok(analyticsService.getCoursePassRate(courseId));
    }

    @GetMapping("/department/{departmentId}/average-score")
    public ResponseEntity<AnalyticsResponse.DepartmentAverageScore> getDepartmentAverageScore(
            @PathVariable Long departmentId) {
        return ResponseEntity.ok(analyticsService.getDepartmentAverageScore(departmentId));
    }

    @GetMapping("/course/{courseId}/overdue")
    public ResponseEntity<List<AnalyticsResponse.OverdueEmployee>> getOverdueEmployees(
            @PathVariable Long courseId) {
        return ResponseEntity.ok(analyticsService.getOverdueEmployees(courseId));
    }

    @GetMapping("/course/{courseId}/most-failed-questions")
    public ResponseEntity<List<AnalyticsResponse.MostFailedQuestion>> getMostFailedQuestions(
            @PathVariable Long courseId) {
        return ResponseEntity.ok(analyticsService.getMostFailedQuestions(courseId));
    }

    // Aggregate dashboard endpoints
    @GetMapping("/overview")
    public ResponseEntity<AnalyticsResponse.OverviewData> getAnalyticsOverview() {
        return ResponseEntity.ok(analyticsService.getAnalyticsOverview());
    }

    @GetMapping("/courses")
    public ResponseEntity<List<AnalyticsResponse.CourseAnalyticsData>> getAllCoursesAnalytics() {
        return ResponseEntity.ok(analyticsService.getAllCoursesAnalytics());
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<AnalyticsResponse.OverdueEnrollmentData>> getAllOverdueEnrollments() {
        return ResponseEntity.ok(analyticsService.getAllOverdueEnrollments());
    }

    @GetMapping("/failed-questions")
    public ResponseEntity<List<AnalyticsResponse.FailedQuestionData>> getMostFailedQuestionsGlobally() {
        return ResponseEntity.ok(analyticsService.getMostFailedQuestionsGlobally());
    }

    @GetMapping("/departments")
    public ResponseEntity<List<AnalyticsResponse.DepartmentAnalyticsData>> getAllDepartmentsAnalytics() {
        return ResponseEntity.ok(analyticsService.getAllDepartmentsAnalytics());
    }
}
