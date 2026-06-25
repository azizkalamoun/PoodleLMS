package com.enterprise.poodle.controller;

import com.enterprise.poodle.dto.request.QCMAttemptFromEnrollmentRequest;
import com.enterprise.poodle.dto.response.QCMAttemptResponse;
import com.enterprise.poodle.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the enrollment attempt endpoints
 * Tests practice and final attempt submissions via enrollment API
 */
@DisplayName("Enrollment Attempt Endpoints")
class EnrollmentAttemptControllerTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Submit practice attempt via enrollment endpoint - returns 200 OK with score")
    void testSubmitPracticeAttempt() throws Exception {
        // Section 3 is Java Practice QCM from BaseIntegrationTest
        Long enrollmentId = assignmentRepository.findAll().get(0).getId();
        Long sectionId = practiceQcmSection.getId();

        // Create practice attempt with answers
        QCMAttemptFromEnrollmentRequest request = QCMAttemptFromEnrollmentRequest.builder()
                .sectionId(sectionId)
                .answers(List.of(
                        QCMAttemptFromEnrollmentRequest.QCMAnswerRequest.builder()
                                .questionId(question3.getId())
                                .selectedAnswerIndex(0) // Answer A
                                .build(),
                        QCMAttemptFromEnrollmentRequest.QCMAnswerRequest.builder()
                                .questionId(question4.getId())
                                .selectedAnswerIndex(1) // Answer B
                                .build()
                ))
                .build();

        // Submit practice attempt
        mockMvc.perform(post("/api/enrollments/{enrollmentId}/practice-attempt", enrollmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").isNumber())
                .andExpect(jsonPath("$.totalQuestions", greaterThan(0)))
                .andExpect(jsonPath("$.passed").isBoolean())
                .andExpect(jsonPath("$.sectionId", equalTo(sectionId.intValue())));
    }

    @Test
    @DisplayName("Submit final attempt via enrollment endpoint - returns 200 OK with passed status")
    void testSubmitFinalAttempt() throws Exception {
        Long enrollmentId = assignmentRepository.findAll().get(0).getId();
        Long finalSectionId = finalQcmSection.getId();

        QCMAttemptFromEnrollmentRequest request = QCMAttemptFromEnrollmentRequest.builder()
                .sectionId(finalSectionId)
                .answers(List.of(
                        QCMAttemptFromEnrollmentRequest.QCMAnswerRequest.builder()
                                .questionId(question1.getId())
                                .selectedAnswerIndex(2) // Answer C
                                .build(),
                        QCMAttemptFromEnrollmentRequest.QCMAnswerRequest.builder()
                                .questionId(question2.getId())
                                .selectedAnswerIndex(0) // Answer A
                                .build()
                ))
                .build();

        // Submit final attempt
        mockMvc.perform(post("/api/enrollments/{enrollmentId}/final-attempt", enrollmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").isNumber())
                .andExpect(jsonPath("$.totalQuestions", greaterThan(0)))
                .andExpect(jsonPath("$.passed").isBoolean())
                .andExpect(jsonPath("$.sectionId", equalTo(finalSectionId.intValue())));
    }

    @Test
    @DisplayName("Return 400 Bad Request for invalid enrollment ID")
    void testPracticeAttemptWithInvalidEnrollmentId() throws Exception {
        Long invalidEnrollmentId = 999L;
        Long sectionId = practiceQcmSection.getId();

        QCMAttemptFromEnrollmentRequest request = QCMAttemptFromEnrollmentRequest.builder()
                .sectionId(sectionId)
                .answers(List.of(
                        QCMAttemptFromEnrollmentRequest.QCMAnswerRequest.builder()
                                .questionId(question1.getId())
                                .selectedAnswerIndex(0)
                                .build()
                ))
                .build();

        mockMvc.perform(post("/api/enrollments/{enrollmentId}/practice-attempt", invalidEnrollmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Return 400 Bad Request for invalid section ID")
    void testPracticeAttemptWithInvalidSectionId() throws Exception {
        Long enrollmentId = assignmentRepository.findAll().get(0).getId();
        Long invalidSectionId = 999L;

        QCMAttemptFromEnrollmentRequest request = QCMAttemptFromEnrollmentRequest.builder()
                .sectionId(invalidSectionId)
                .answers(List.of(
                        QCMAttemptFromEnrollmentRequest.QCMAnswerRequest.builder()
                                .questionId(question1.getId())
                                .selectedAnswerIndex(0)
                                .build()
                ))
                .build();

        mockMvc.perform(post("/api/enrollments/{enrollmentId}/practice-attempt", enrollmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Return 401 Unauthorized without valid token")
    void testPracticeAttemptWithoutAuthorization() throws Exception {
        Long enrollmentId = assignmentRepository.findAll().get(0).getId();
        Long sectionId = practiceQcmSection.getId();

        QCMAttemptFromEnrollmentRequest request = QCMAttemptFromEnrollmentRequest.builder()
                .sectionId(sectionId)
                .answers(List.of(
                        QCMAttemptFromEnrollmentRequest.QCMAnswerRequest.builder()
                                .questionId(question1.getId())
                                .selectedAnswerIndex(0)
                                .build()
                ))
                .build();

        mockMvc.perform(post("/api/enrollments/{enrollmentId}/practice-attempt", enrollmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Return 400 Bad Request for invalid answer index (out of range)")
    void testPracticeAttemptWithInvalidAnswerIndex() throws Exception {
        Long enrollmentId = assignmentRepository.findAll().get(0).getId();
        Long sectionId = practiceQcmSection.getId();

        QCMAttemptFromEnrollmentRequest request = QCMAttemptFromEnrollmentRequest.builder()
                .sectionId(sectionId)
                .answers(List.of(
                        QCMAttemptFromEnrollmentRequest.QCMAnswerRequest.builder()
                                .questionId(question1.getId())
                                .selectedAnswerIndex(5) // Invalid: should be 0-3
                                .build()
                ))
                .build();

        mockMvc.perform(post("/api/enrollments/{enrollmentId}/practice-attempt", enrollmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isBadRequest());
    }
}
