package com.enterprise.poodle.controller;

import com.enterprise.poodle.BaseIntegrationTest;
import com.enterprise.poodle.dto.request.SectionRequest;
import com.enterprise.poodle.enums.ContentType;
import com.enterprise.poodle.enums.QcmType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Section Controller Tests")
class SectionControllerTest extends BaseIntegrationTest {

    @Nested
    @DisplayName("POST /api/courses/{courseId}/sections")
    class CreateSectionTests {

        @Test
        @DisplayName("Should create a video section")
        void createVideoSection() throws Exception {
            SectionRequest request = new SectionRequest();
            request.setTitle("New Video Lesson");
            request.setContentType(ContentType.VIDEO);
            request.setContentUrl("https://example.com/video2.mp4");
            request.setOrderIndex(10);

            mockMvc.perform(post("/api/courses/{courseId}/sections", publishedCourse.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.title").value("New Video Lesson"))
                    .andExpect(jsonPath("$.contentType").value("VIDEO"))
                    .andExpect(jsonPath("$.orderIndex").value(10));
        }

        @Test
        @DisplayName("Should create a practice QCM section")
        void createPracticeQcmSection() throws Exception {
            SectionRequest request = new SectionRequest();
            request.setTitle("Practice Quiz 2");
            request.setContentType(ContentType.QCM);
            request.setQcmType(QcmType.PRACTICE);
            request.setOrderIndex(5);
            request.setMaxAttempts(3);

            mockMvc.perform(post("/api/courses/{courseId}/sections", publishedCourse.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.contentType").value("QCM"))
                    .andExpect(jsonPath("$.qcmType").value("PRACTICE"))
                    .andExpect(jsonPath("$.maxAttempts").value(3));
        }

        @Test
        @DisplayName("Should reject duplicate FINAL QCM section")
        void createDuplicateFinalQcm() throws Exception {
            SectionRequest request = new SectionRequest();
            request.setTitle("Another Final");
            request.setContentType(ContentType.QCM);
            request.setQcmType(QcmType.FINAL);

            mockMvc.perform(post("/api/courses/{courseId}/sections", publishedCourse.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 for non-existent course")
        void createSectionCourseNotFound() throws Exception {
            SectionRequest request = new SectionRequest();
            request.setTitle("Orphan Section");
            request.setContentType(ContentType.TEXT);

            mockMvc.perform(post("/api/courses/{courseId}/sections", 99999)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 for employee")
        void createSectionForbidden() throws Exception {
            SectionRequest request = new SectionRequest();
            request.setTitle("Denied");
            request.setContentType(ContentType.TEXT);

            mockMvc.perform(post("/api/courses/{courseId}/sections", publishedCourse.getId())
                            .header("Authorization", "Bearer " + employeeToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/courses/{courseId}/sections")
    class GetSectionsTests {

        @Test
        @DisplayName("Should return sections for a course")
        void getSectionsByCourse() throws Exception {
            mockMvc.perform(get("/api/courses/{courseId}/sections", publishedCourse.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].title").value("Intro Video"))
                    .andExpect(jsonPath("$[1].title").value("Practice Quiz"))
                    .andExpect(jsonPath("$[2].title").value("Final Exam"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent course")
        void getSectionsNotFound() throws Exception {
            mockMvc.perform(get("/api/courses/{courseId}/sections", 99999)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/sections/{sectionId}")
    class UpdateSectionTests {

        @Test
        @DisplayName("Should update section title and URL")
        void updateSection() throws Exception {
            SectionRequest request = new SectionRequest();
            request.setTitle("Updated Video Title");
            request.setContentType(ContentType.VIDEO);
            request.setContentUrl("https://example.com/updated.mp4");

            mockMvc.perform(put("/api/sections/{sectionId}", videoSection.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Updated Video Title"))
                    .andExpect(jsonPath("$.contentUrl").value("https://example.com/updated.mp4"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent section")
        void updateSectionNotFound() throws Exception {
            SectionRequest request = new SectionRequest();
            request.setTitle("Ghost");
            request.setContentType(ContentType.TEXT);

            mockMvc.perform(put("/api/sections/{sectionId}", 99999)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/sections/{sectionId}")
    class DeleteSectionTests {

        @Test
        @DisplayName("Should delete section")
        void deleteSection() throws Exception {
            mockMvc.perform(delete("/api/sections/{sectionId}", videoSection.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 404 for non-existent section")
        void deleteSectionNotFound() throws Exception {
            mockMvc.perform(delete("/api/sections/{sectionId}", 99999)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }
}
