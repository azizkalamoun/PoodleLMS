package com.enterprise.poodle.controller;

import com.enterprise.poodle.BaseIntegrationTest;
import com.enterprise.poodle.dto.request.CourseAssignmentRequest;
import com.enterprise.poodle.dto.request.CourseRequest;
import com.enterprise.poodle.enums.CourseStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Course Controller Tests")
class CourseControllerTest extends BaseIntegrationTest {

    @Nested
    @DisplayName("POST /api/courses")
    class CreateCourseTests {

        @Test
        @DisplayName("Should create a course as admin")
        void createCourse() throws Exception {
            CourseRequest request = new CourseRequest();
            request.setTitle("New Course");
            request.setDescription("Course description");
            request.setStatus(CourseStatus.DRAFT);
            request.setPassingScore(80);

            mockMvc.perform(post("/api/courses")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.title").value("New Course"))
                    .andExpect(jsonPath("$.description").value("Course description"))
                    .andExpect(jsonPath("$.status").value("DRAFT"))
                    .andExpect(jsonPath("$.passingScore").value(80));
        }

        @Test
        @DisplayName("Should create a course with prerequisites")
        void createCourseWithPrerequisites() throws Exception {
            CourseRequest request = new CourseRequest();
            request.setTitle("Advanced Course");
            request.setDescription("Requires published course");
            request.setPrerequisiteCourseIds(java.util.List.of(publishedCourse.getId()));

            mockMvc.perform(post("/api/courses")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.prerequisiteCourseIds[0]").value(publishedCourse.getId()));
        }

        @Test
        @DisplayName("Should return 403 for employee")
        void createCourseForbidden() throws Exception {
            CourseRequest request = new CourseRequest();
            request.setTitle("Denied");

            mockMvc.perform(post("/api/courses")
                            .header("Authorization", "Bearer " + employeeToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 400 for missing title")
        void createCourseMissingTitle() throws Exception {
            CourseRequest request = new CourseRequest();
            request.setDescription("No title");

            mockMvc.perform(post("/api/courses")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/courses")
    class GetAllCoursesTests {

        @Test
        @DisplayName("Should return all courses for admin")
        void getAllCoursesAdmin() throws Exception {
            mockMvc.perform(get("/api/courses")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(2)));
        }

        @Test
        @DisplayName("Should return assigned courses for employee")
        void getAssignedCoursesEmployee() throws Exception {
            mockMvc.perform(get("/api/courses")
                            .header("Authorization", "Bearer " + employeeToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/courses/{id}")
    class GetCourseByIdTests {

        @Test
        @DisplayName("Should return course by ID for admin")
        void getCourseByIdAdmin() throws Exception {
            mockMvc.perform(get("/api/courses/{id}", publishedCourse.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(publishedCourse.getId()))
                    .andExpect(jsonPath("$.title").value("Test Course"));
        }

        @Test
        @DisplayName("Should return published course for assigned employee")
        void getCourseByIdEmployee() throws Exception {
            mockMvc.perform(get("/api/courses/{id}", publishedCourse.getId())
                            .header("Authorization", "Bearer " + employeeToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Test Course"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent course")
        void getCourseNotFound() throws Exception {
            mockMvc.perform(get("/api/courses/{id}", 99999)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/courses/{id}")
    class UpdateCourseTests {

        @Test
        @DisplayName("Should update course as admin")
        void updateCourse() throws Exception {
            CourseRequest request = new CourseRequest();
            request.setTitle("Updated Course Title");
            request.setStatus(CourseStatus.PUBLISHED);
            request.setPassingScore(85);

            mockMvc.perform(put("/api/courses/{id}", publishedCourse.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Updated Course Title"))
                    .andExpect(jsonPath("$.passingScore").value(85));
        }

        @Test
        @DisplayName("Should return 403 for employee")
        void updateCourseForbidden() throws Exception {
            CourseRequest request = new CourseRequest();
            request.setTitle("Hacked");

            mockMvc.perform(put("/api/courses/{id}", publishedCourse.getId())
                            .header("Authorization", "Bearer " + employeeToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/courses/{id}")
    class DeleteCourseTests {

        @Test
        @DisplayName("Should soft-delete course as admin")
        void deleteCourse() throws Exception {
            mockMvc.perform(delete("/api/courses/{id}", draftCourse.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/courses/{id}", draftCourse.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 for non-existent course")
        void deleteNonExistentCourse() throws Exception {
            mockMvc.perform(delete("/api/courses/{id}", 99999)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/courses/{courseId}/assign/{departmentId}")
    class AssignCourseTests {

        @Test
        @DisplayName("Should assign course to department")
        void assignCourseToDepartment() throws Exception {
            CourseAssignmentRequest request = new CourseAssignmentRequest();
            request.setDeadlineDate(LocalDate.now().plusMonths(3));

            mockMvc.perform(post("/api/courses/{courseId}/assign/{departmentId}",
                            publishedCourse.getId(), hr.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.courseId").value(publishedCourse.getId()))
                    .andExpect(jsonPath("$.departmentId").value(hr.getId()))
                    .andExpect(jsonPath("$.courseTitle").value("Test Course"));
        }

        @Test
        @DisplayName("Should return 409 for duplicate assignment")
        void assignCourseDuplicate() throws Exception {
            CourseAssignmentRequest request = new CourseAssignmentRequest();
            request.setDeadlineDate(LocalDate.now().plusMonths(3));

            // backend is already assigned
            mockMvc.perform(post("/api/courses/{courseId}/assign/{departmentId}",
                            publishedCourse.getId(), backend.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("GET /api/courses/{courseId}/assigned-departments")
    class GetAssignedDepartmentsTests {

        @Test
        @DisplayName("Should return assigned departments")
        void getAssignedDepartments() throws Exception {
            mockMvc.perform(get("/api/courses/{courseId}/assigned-departments",
                            publishedCourse.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].departmentName").value("Backend"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent course")
        void getAssignedDepartmentsNotFound() throws Exception {
            mockMvc.perform(get("/api/courses/{courseId}/assigned-departments", 99999)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }
}
