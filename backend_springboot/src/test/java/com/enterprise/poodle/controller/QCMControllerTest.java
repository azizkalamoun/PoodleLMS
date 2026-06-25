package com.enterprise.poodle.controller;

import com.enterprise.poodle.BaseIntegrationTest;
import com.enterprise.poodle.dto.request.QCMAttemptRequest;
import com.enterprise.poodle.dto.request.QCMQuestionRequest;
import com.enterprise.poodle.entity.CourseSection;
import com.enterprise.poodle.entity.QCMQuestion;
import com.enterprise.poodle.enums.ContentType;
import com.enterprise.poodle.enums.QcmType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("QCM Controller Tests")
class QCMControllerTest extends BaseIntegrationTest {

    @Nested
    @DisplayName("POST /api/sections/{sectionId}/questions")
    class CreateQuestionTests {

        @Test
        @DisplayName("Should create a question in QCM section")
        void createQuestion() throws Exception {
            QCMQuestionRequest request = new QCMQuestionRequest();
            request.setQuestionText("What is polymorphism?");
            request.setOptionA("A type of variable");
            request.setOptionB("A design pattern");
            request.setOptionC("The ability to take many forms");
            request.setOptionD("A data structure");
            request.setCorrectOption("C");

            mockMvc.perform(post("/api/sections/{sectionId}/questions", finalQcmSection.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.questionText").value("What is polymorphism?"))
                    .andExpect(jsonPath("$.correctOption").value("C"))
                    .andExpect(jsonPath("$.llmGenerated").value(false));
        }

        @Test
        @DisplayName("Should reject question on non-QCM section")
        void createQuestionOnNonQcmSection() throws Exception {
            QCMQuestionRequest request = new QCMQuestionRequest();
            request.setQuestionText("Invalid");
            request.setOptionA("A");
            request.setOptionB("B");
            request.setOptionC("C");
            request.setOptionD("D");
            request.setCorrectOption("A");

            mockMvc.perform(post("/api/sections/{sectionId}/questions", videoSection.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for invalid correct option")
        void createQuestionInvalidOption() throws Exception {
            QCMQuestionRequest request = new QCMQuestionRequest();
            request.setQuestionText("Bad question");
            request.setOptionA("A");
            request.setOptionB("B");
            request.setOptionC("C");
            request.setOptionD("D");
            request.setCorrectOption("E"); // invalid

            mockMvc.perform(post("/api/sections/{sectionId}/questions", finalQcmSection.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 403 for employee")
        void createQuestionForbidden() throws Exception {
            QCMQuestionRequest request = new QCMQuestionRequest();
            request.setQuestionText("Forbidden");
            request.setOptionA("A");
            request.setOptionB("B");
            request.setOptionC("C");
            request.setOptionD("D");
            request.setCorrectOption("A");

            mockMvc.perform(post("/api/sections/{sectionId}/questions", finalQcmSection.getId())
                            .header("Authorization", "Bearer " + employeeToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/sections/{sectionId}/questions")
    class GetQuestionsTests {

        @Test
        @DisplayName("Should return questions with correct options for admin")
        void getQuestionsAdmin() throws Exception {
            mockMvc.perform(get("/api/sections/{sectionId}/questions", finalQcmSection.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].correctOption").isNotEmpty());
        }

        @Test
        @DisplayName("Should hide correct options for employee")
        void getQuestionsEmployee() throws Exception {
            mockMvc.perform(get("/api/sections/{sectionId}/questions", finalQcmSection.getId())
                            .header("Authorization", "Bearer " + employeeToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].correctOption").isEmpty());
        }

        @Test
        @DisplayName("Should return 404 for non-existent section")
        void getQuestionsNotFound() throws Exception {
            mockMvc.perform(get("/api/sections/{sectionId}/questions", 99999)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/sections/{sectionId}/attempt")
    class SubmitAttemptTests {

        @Test
        @DisplayName("Should submit an attempt and get score")
        void submitAttempt() throws Exception {
            QCMAttemptRequest request = new QCMAttemptRequest();
            request.setAnswers(Map.of(
                    question1.getId(), "B",  // correct
                    question2.getId(), "C"   // correct
            ));

            mockMvc.perform(post("/api/sections/{sectionId}/attempt", finalQcmSection.getId())
                            .header("Authorization", "Bearer " + employeeToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.score").value(100))
                    .andExpect(jsonPath("$.totalQuestions").value(2))
                    .andExpect(jsonPath("$.attemptNumber").value(1))
                    .andExpect(jsonPath("$.passed").value(true));
        }

        @Test
        @DisplayName("Should track partial score")
        void submitAttemptPartialScore() throws Exception {
            QCMAttemptRequest request = new QCMAttemptRequest();
            request.setAnswers(Map.of(
                    question1.getId(), "A",  // wrong (correct: B)
                    question2.getId(), "C"   // correct
            ));

            mockMvc.perform(post("/api/sections/{sectionId}/attempt", finalQcmSection.getId())
                            .header("Authorization", "Bearer " + employeeToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.score").value(50))
                    .andExpect(jsonPath("$.passed").value(false));
        }

        @Test
        @DisplayName("Should return 403 for admin (employee-only)")
        void submitAttemptForbiddenForAdmin() throws Exception {
            QCMAttemptRequest request = new QCMAttemptRequest();
            request.setAnswers(Map.of(question1.getId(), "B"));

            mockMvc.perform(post("/api/sections/{sectionId}/attempt", finalQcmSection.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should reject attempt on non-QCM section")
        void submitAttemptOnNonQcm() throws Exception {
            QCMAttemptRequest request = new QCMAttemptRequest();
            request.setAnswers(Map.of(1L, "A"));

            mockMvc.perform(post("/api/sections/{sectionId}/attempt", videoSection.getId())
                            .header("Authorization", "Bearer " + employeeToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/sections/{sectionId}/generate-qcm-draft")
    class GenerateQCMDraftTests {

        @Test
        @DisplayName("Should generate QCM draft for text section with LLM enabled")
        void generateDraft() throws Exception {
            // Create a text section with LLM enabled
            CourseSection textSection = sectionRepository.save(CourseSection.builder()
                    .course(publishedCourse).title("Text Content")
                    .contentType(ContentType.QCM).qcmType(QcmType.PRACTICE).contentUrl("https://example.com/content")
                    .orderIndex(10).llmDraftEnabled(true).build());

            mockMvc.perform(post("/api/sections/{sectionId}/generate-qcm-draft", textSection.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].llmGenerated").value(true));
        }

        @Test
        @DisplayName("Should reject LLM draft for video section")
        void generateDraftInvalidContentType() throws Exception {
            mockMvc.perform(post("/api/sections/{sectionId}/generate-qcm-draft", videoSection.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 403 for employee")
        void generateDraftForbidden() throws Exception {
            mockMvc.perform(post("/api/sections/{sectionId}/generate-qcm-draft", finalQcmSection.getId())
                            .header("Authorization", "Bearer " + employeeToken))
                    .andExpect(status().isForbidden());
        }
    }
}
