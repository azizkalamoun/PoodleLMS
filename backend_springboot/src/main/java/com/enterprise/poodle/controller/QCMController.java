package com.enterprise.poodle.controller;

import com.enterprise.poodle.dto.request.QCMAttemptRequest;
import com.enterprise.poodle.dto.request.QCMQuestionRequest;
import com.enterprise.poodle.dto.request.GenerateQCMDraftRequest;
import com.enterprise.poodle.dto.response.AttemptCountResponse;
import com.enterprise.poodle.dto.response.QCMAttemptResponse;
import com.enterprise.poodle.dto.response.QCMQuestionResponse;
import com.enterprise.poodle.service.LLMService;
import com.enterprise.poodle.service.QCMService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
public class QCMController {

    private final QCMService qcmService;
    private final LLMService llmService;

    @PostMapping("/{sectionId}/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QCMQuestionResponse> createQuestion(
            @PathVariable Long sectionId,
            @Valid @RequestBody QCMQuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(qcmService.createQuestion(sectionId, request));
    }

    @GetMapping("/{sectionId}/questions")
    public ResponseEntity<List<QCMQuestionResponse>> getQuestions(
            @PathVariable Long sectionId) {
        return ResponseEntity.ok(qcmService.getQuestions(sectionId));
    }

    @PostMapping("/{sectionId}/attempt")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<QCMAttemptResponse> submitAttempt(
            @PathVariable Long sectionId,
            @Valid @RequestBody QCMAttemptRequest request) {
        return ResponseEntity.ok(qcmService.submitAttempt(sectionId, request));
    }

    @PutMapping("/{sectionId}/questions/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QCMQuestionResponse> updateQuestion(
            @PathVariable Long sectionId,
            @PathVariable Long questionId,
            @Valid @RequestBody QCMQuestionRequest request) {
        return ResponseEntity.ok(qcmService.updateQuestion(sectionId, questionId, request));
    }

    @DeleteMapping("/{sectionId}/questions/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long sectionId,
            @PathVariable Long questionId) {
        qcmService.deleteQuestion(sectionId, questionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{sectionId}/my-attempt-count")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<AttemptCountResponse> getMyAttemptCount(@PathVariable Long sectionId) {
        return ResponseEntity.ok(qcmService.getMyAttemptCount(sectionId));
    }

    @PostMapping("/{sectionId}/generate-qcm-draft")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<QCMQuestionResponse>> generateQCMDraft(
            @PathVariable Long sectionId,
            @RequestBody(required = false) GenerateQCMDraftRequest request) {
        Integer numQuestions = (request != null && request.getNumberOfQuestions() != null) 
            ? request.getNumberOfQuestions() 
            : 5;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(llmService.generateQCMDraft(sectionId, numQuestions));
    }
}
