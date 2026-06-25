package com.enterprise.poodle.controller;

import com.enterprise.poodle.BaseIntegrationTest;
import com.enterprise.poodle.entity.*;
import com.enterprise.poodle.enums.ProgressStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Analytics Controller Tests")
class AnalyticsControllerTest extends BaseIntegrationTest {

    @Nested
    @DisplayName("GET /api/analytics/course/{courseId}/completion")
    class CompletionRateTests {

        @Test
        @DisplayName("Should return completion rate for a course")
        void getCompletionRate() throws Exception {
            mockMvc.perform(get("/api/analytics/course/{courseId}/completion",
                            publishedCourse.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.courseId").value(publishedCourse.getId()))
                    .andExpect(jsonPath("$.courseTitle").value("Test Course"))
                    .andExpect(jsonPath("$.totalAssigned").isNumber())
                    .andExpect(jsonPath("$.completionRate").isNumber());
        }

        @Test
        @DisplayName("Should return 404 for non-existent course")
        void getCompletionRateNotFound() throws Exception {
            mockMvc.perform(get("/api/analytics/course/{courseId}/completion", 99999)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 for employee")
        void getCompletionRateForbidden() throws Exception {
            mockMvc.perform(get("/api/analytics/course/{courseId}/completion",
                            publishedCourse.getId())
                            .header("Authorization", "Bearer " + employeeToken))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/analytics/course/{courseId}/pass-rate")
    class PassRateTests {

        @Test
        @DisplayName("Should return pass rate for a course")
        void getPassRate() throws Exception {
            // Add some grades
            gradeRepository.save(EmployeeCourseGrade.builder()
                    .employee(employeeUser).course(publishedCourse)
                    .finalScore(85).passed(true)
                    .updatedAt(LocalDateTime.now()).build());

            mockMvc.perform(get("/api/analytics/course/{courseId}/pass-rate",
                            publishedCourse.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.courseId").value(publishedCourse.getId()))
                    .andExpect(jsonPath("$.totalAttempted").value(1))
                    .andExpect(jsonPath("$.totalPassed").value(1))
                    .andExpect(jsonPath("$.passRate").value(100.0));
        }

        @Test
        @DisplayName("Should return 0% when no grades exist")
        void getPassRateNoGrades() throws Exception {
            mockMvc.perform(get("/api/analytics/course/{courseId}/pass-rate",
                            publishedCourse.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.passRate").value(0.0));
        }
    }

    @Nested
    @DisplayName("GET /api/analytics/department/{departmentId}/average-score")
    class DepartmentAverageScoreTests {

        @Test
        @DisplayName("Should return average score for department")
        void getDepartmentAverageScore() throws Exception {
            gradeRepository.save(EmployeeCourseGrade.builder()
                    .employee(employeeUser).course(publishedCourse)
                    .finalScore(80).passed(true)
                    .updatedAt(LocalDateTime.now()).build());

            mockMvc.perform(get("/api/analytics/department/{departmentId}/average-score",
                            backend.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.departmentId").value(backend.getId()))
                    .andExpect(jsonPath("$.departmentName").value("Backend"))
                    .andExpect(jsonPath("$.averageScore").value(80.0))
                    .andExpect(jsonPath("$.totalGraded").value(1));
        }

        @Test
        @DisplayName("Should return 0 average when no grades")
        void getDepartmentAverageScoreNoGrades() throws Exception {
            mockMvc.perform(get("/api/analytics/department/{departmentId}/average-score",
                            hr.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.averageScore").value(0.0))
                    .andExpect(jsonPath("$.totalGraded").value(0));
        }

        @Test
        @DisplayName("Should return 404 for non-existent department")
        void getDepartmentAverageScoreNotFound() throws Exception {
            mockMvc.perform(get("/api/analytics/department/{departmentId}/average-score", 99999)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/analytics/course/{courseId}/overdue")
    class OverdueEmployeesTests {

        @Test
        @DisplayName("Should return overdue employees")
        void getOverdueEmployees() throws Exception {
            // Create an overdue assignment for backend dept
            assignmentRepository.save(CourseDepartmentAssignment.builder()
                    .course(draftCourse).department(backend)
                    .deadlineDate(LocalDate.now().minusDays(10)).build());

            mockMvc.perform(get("/api/analytics/course/{courseId}/overdue",
                            draftCourse.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Should return 404 for non-existent course")
        void getOverdueEmployeesNotFound() throws Exception {
            mockMvc.perform(get("/api/analytics/course/{courseId}/overdue", 99999)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/analytics/course/{courseId}/most-failed-questions")
    class MostFailedQuestionsTests {

        @Test
        @DisplayName("Should return most failed questions")
        void getMostFailedQuestions() throws Exception {
            mockMvc.perform(get("/api/analytics/course/{courseId}/most-failed-questions",
                            publishedCourse.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Should return 404 for non-existent course")
        void getMostFailedQuestionsNotFound() throws Exception {
            mockMvc.perform(get("/api/analytics/course/{courseId}/most-failed-questions", 99999)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 for employee")
        void getMostFailedQuestionsForbidden() throws Exception {
            mockMvc.perform(get("/api/analytics/course/{courseId}/most-failed-questions",
                            publishedCourse.getId())
                            .header("Authorization", "Bearer " + employeeToken))
                    .andExpect(status().isForbidden());
        }
    }
}
