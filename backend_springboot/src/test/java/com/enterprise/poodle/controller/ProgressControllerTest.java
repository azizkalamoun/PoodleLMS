package com.enterprise.poodle.controller;

import com.enterprise.poodle.BaseIntegrationTest;
import com.enterprise.poodle.entity.EmployeeCourseProgress;
import com.enterprise.poodle.enums.ProgressStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Progress Controller Tests")
class ProgressControllerTest extends BaseIntegrationTest {

    @Nested
    @DisplayName("GET /api/me/courses")
    class GetMyCoursesTests {

        @Test
        @DisplayName("Should return assigned courses for employee")
        void getMyCourses() throws Exception {
            mockMvc.perform(get("/api/me/courses")
                            .header("Authorization", "Bearer " + employeeToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should return 403 for admin")
        void getMyCoursesForbiddenForAdmin() throws Exception {
            mockMvc.perform(get("/api/me/courses")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 without token")
        void getMyCoursesUnauthorized() throws Exception {
            mockMvc.perform(get("/api/me/courses"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/me/progress/{courseId}")
    class GetCourseProgressTests {

        @Test
        @DisplayName("Should return progress for a course")
        void getCourseProgress() throws Exception {
            // Add some progress for employeeUser
            progressRepository.save(EmployeeCourseProgress.builder()
                    .employee(employeeUser).course(publishedCourse).section(videoSection)
                    .status(ProgressStatus.COMPLETED)
                    .completedAt(LocalDateTime.now().minusDays(1)).build());

            progressRepository.save(EmployeeCourseProgress.builder()
                    .employee(employeeUser).course(publishedCourse).section(practiceQcmSection)
                    .status(ProgressStatus.IN_PROGRESS).build());

            mockMvc.perform(get("/api/me/progress/{courseId}", publishedCourse.getId())
                            .header("Authorization", "Bearer " + employeeToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.courseId").value(publishedCourse.getId()))
                    .andExpect(jsonPath("$.courseTitle").value("Test Course"))
                    .andExpect(jsonPath("$.totalSections").value(3))
                    .andExpect(jsonPath("$.completedSections").value(1))
                    .andExpect(jsonPath("$.sectionProgress").isArray())
                    .andExpect(jsonPath("$.sectionProgress.length()").value(3));
        }

        @Test
        @DisplayName("Should return progress with zero completion when no progress")
        void getCourseProgressEmpty() throws Exception {
            mockMvc.perform(get("/api/me/progress/{courseId}", publishedCourse.getId())
                            .header("Authorization", "Bearer " + employeeToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.completedSections").value(0))
                    .andExpect(jsonPath("$.completionPercentage").value(0.0));
        }

        @Test
        @DisplayName("Should return 404 for non-existent course")
        void getCourseProgressNotFound() throws Exception {
            mockMvc.perform(get("/api/me/progress/{courseId}", 99999)
                            .header("Authorization", "Bearer " + employeeToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 for draft course")
        void getCourseProgressDraftCourse() throws Exception {
            mockMvc.perform(get("/api/me/progress/{courseId}", draftCourse.getId())
                            .header("Authorization", "Bearer " + employeeToken))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 403 for admin")
        void getCourseProgressForbiddenForAdmin() throws Exception {
            mockMvc.perform(get("/api/me/progress/{courseId}", publishedCourse.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isForbidden());
        }
    }
}
