package com.enterprise.poodle.controller;

import com.enterprise.poodle.dto.request.SectionRequest;
import com.enterprise.poodle.dto.response.SectionResponse;
import com.enterprise.poodle.dto.response.QCMQuestionResponse;
import com.enterprise.poodle.service.SectionService;
import com.enterprise.poodle.service.LLMService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;
    private final LLMService llmService;

    @PostMapping("/courses/{courseId}/sections/{sectionId}/generate-qcm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<QCMQuestionResponse>> generateQCMDraft(
            @PathVariable Long courseId,
            @PathVariable Long sectionId,
            @RequestBody(required = false) Map<String, Object> request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(llmService.generateQCMDraft(sectionId));
    }

    @PostMapping("/courses/{courseId}/sections")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SectionResponse> createSection(
            @PathVariable Long courseId,
            @Valid @RequestBody SectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sectionService.createSection(courseId, request));
    }

    @GetMapping("/courses/{courseId}/sections")
    public ResponseEntity<List<SectionResponse>> getSectionsByCourse(
            @PathVariable Long courseId) {
        return ResponseEntity.ok(sectionService.getSectionsByCourse(courseId));
    }

    @PutMapping("/sections/{sectionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SectionResponse> updateSection(
            @PathVariable Long sectionId,
            @Valid @RequestBody SectionRequest request) {
        return ResponseEntity.ok(sectionService.updateSection(sectionId, request));
    }

    @DeleteMapping("/sections/{sectionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SectionResponse> deleteSection(@PathVariable Long sectionId) {
        // Return the deleted section info (includes contentUrl for frontend asset cleanup)
        return ResponseEntity.ok(sectionService.deleteSectionAndReturnInfo(sectionId));
    }
}
