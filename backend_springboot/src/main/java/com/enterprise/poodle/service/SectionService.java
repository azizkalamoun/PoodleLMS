package com.enterprise.poodle.service;

import com.enterprise.poodle.dto.request.SectionRequest;
import com.enterprise.poodle.dto.response.SectionResponse;
import com.enterprise.poodle.entity.Course;
import com.enterprise.poodle.entity.CourseSection;
import com.enterprise.poodle.enums.ActionType;
import com.enterprise.poodle.enums.ContentType;
import com.enterprise.poodle.enums.QcmType;
import com.enterprise.poodle.exception.BusinessException;
import com.enterprise.poodle.exception.ResourceNotFoundException;
import com.enterprise.poodle.repository.CourseRepository;
import com.enterprise.poodle.repository.CourseSectionRepository;
import com.enterprise.poodle.repository.QCMQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SectionService {

    private final CourseSectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final QCMQuestionRepository questionRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public SectionResponse createSection(Long courseId, SectionRequest request) {
        log.info("Creating section for course: {}", courseId);
        log.debug("Section request: title={}, contentType={}, contentUrl={}", 
                request.getTitle(), request.getContentType(), request.getContentUrl());
        
        Course course = courseRepository.findByIdAndDeletedFalse(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // Validate request
        if (request == null) {
            throw new BusinessException("Section request cannot be null");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new BusinessException("Section title is required");
        }
        if (request.getContentType() == null) {
            throw new BusinessException("Content type is required");
        }

        // Validate FINAL QCM uniqueness
        if (request.getContentType() == ContentType.QCM
                && request.getQcmType() == QcmType.FINAL
                && sectionRepository.existsByCourseIdAndQcmType(courseId, QcmType.FINAL)) {
            throw new BusinessException("Course already has a FINAL QCM section");
        }

        CourseSection section = CourseSection.builder()
                .course(course)
                .title(request.getTitle())
                .contentType(request.getContentType())
                .contentUrl(request.getContentUrl())
                .fileDescription(request.getFileDescription())
                .orderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0)
                .qcmType(request.getContentType() == ContentType.QCM ? request.getQcmType() : null)
                .maxAttempts(request.getMaxAttempts())
                .llmDraftEnabled(request.getLlmDraftEnabled() != null && request.getLlmDraftEnabled())
                .build();

        log.debug("Saving section: {}", section.getTitle());
        CourseSection saved = sectionRepository.save(section);
        
        // Build response while still within transaction (course is still accessible)
        // Accessing lazy-loaded course must happen within @Transactional boundary
        SectionResponse response = SectionResponse.builder()
                .id(saved.getId())
                .courseId(saved.getCourse().getId())
                .title(saved.getTitle())
                .contentType(saved.getContentType())
                .contentUrl(saved.getContentUrl())
                .fileDescription(saved.getFileDescription())
                .orderIndex(saved.getOrderIndex())
                .qcmType(saved.getQcmType())
                .maxAttempts(saved.getMaxAttempts())
                .llmDraftEnabled(saved.isLlmDraftEnabled())
                .questionCount((int) questionRepository.countBySectionId(saved.getId()))
                .build();
        
        log.info("Section created successfully: id={}, title={}", saved.getId(), saved.getTitle());
        return response;
    }

    @Transactional(readOnly = true)
    public List<SectionResponse> getSectionsByCourse(Long courseId) {
        courseRepository.findByIdAndDeletedFalse(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        return sectionRepository.findByCourseIdOrderByOrderIndexAsc(courseId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SectionResponse updateSection(Long sectionId, SectionRequest request) {
        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));

        Map<String, Object> oldValues = Map.of(
                "title", section.getTitle(),
                "contentType", section.getContentType());

        // Get courseId early to avoid lazy loading issues
        Long courseId = section.getCourse().getId();

        // Validate FINAL QCM uniqueness when changing type
        if (request.getContentType() == ContentType.QCM
                && request.getQcmType() == QcmType.FINAL
                && section.getQcmType() != QcmType.FINAL) {
            if (sectionRepository.existsByCourseIdAndQcmType(courseId, QcmType.FINAL)) {
                throw new BusinessException("Course already has a FINAL QCM section");
            }
        }

        if (request.getTitle() != null) section.setTitle(request.getTitle());
        if (request.getContentType() != null) section.setContentType(request.getContentType());
        if (request.getFileDescription() != null) section.setFileDescription(request.getFileDescription());
        // Handle contentUrl - allow clearing it by setting to null
        if (request.getContentUrl() != null) {
            section.setContentUrl(request.getContentUrl());
        } else if (request.getContentType() != null && 
                   (request.getContentType() == ContentType.QCM || 
                    request.getContentType() == ContentType.TEXT)) {
            // For QCM and TEXT types, contentUrl can be null
            // Keep existing value or allow clearing
        }
        if (request.getOrderIndex() != null) section.setOrderIndex(request.getOrderIndex());
        if (request.getContentType() == ContentType.QCM) {
            section.setQcmType(request.getQcmType());
        } else {
            // Clear QCM type if not a QCM section
            section.setQcmType(null);
        }
        if (request.getMaxAttempts() != null) section.setMaxAttempts(request.getMaxAttempts());
        if (request.getLlmDraftEnabled() != null) section.setLlmDraftEnabled(request.getLlmDraftEnabled());

        CourseSection saved = sectionRepository.save(section);

        auditLogService.logAction(ActionType.UPDATE, "Section", saved.getId(),
                oldValues, Map.of("title", saved.getTitle(), "contentType", saved.getContentType()));

        return mapToResponse(saved);
    }

    @Transactional
    public void deleteSection(Long sectionId) {
        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));
        sectionRepository.delete(section);
    }

    @Transactional
    public SectionResponse deleteSectionAndReturnInfo(Long sectionId) {
        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));
        
        // Create response before deletion (while still within transaction)
        // Accessing lazy-loaded course must happen within @Transactional boundary
        SectionResponse response = SectionResponse.builder()
                .id(section.getId())
                .courseId(section.getCourse().getId())
                .title(section.getTitle())
                .contentType(section.getContentType())
                .contentUrl(section.getContentUrl())
                .orderIndex(section.getOrderIndex())
                .qcmType(section.getQcmType())
                .maxAttempts(section.getMaxAttempts())
                .llmDraftEnabled(section.isLlmDraftEnabled())
                .questionCount((int) questionRepository.countBySectionId(section.getId()))
                .build();
        
        // Delete the section
        sectionRepository.delete(section);
        
        // Log the deletion
        auditLogService.logAction(ActionType.DELETE, "Section", sectionId,
                Map.of("title", section.getTitle(), "contentUrl", section.getContentUrl() != null ? section.getContentUrl() : "N/A"),
                Map.of());
        
        return response;
    }

    private SectionResponse mapToResponse(CourseSection section) {
        return SectionResponse.builder()
                .id(section.getId())
                .courseId(section.getCourse().getId())
                .title(section.getTitle())
                .contentType(section.getContentType())
                .contentUrl(section.getContentUrl())
                .fileDescription(section.getFileDescription())
                .orderIndex(section.getOrderIndex())
                .qcmType(section.getQcmType())
                .maxAttempts(section.getMaxAttempts())
                .llmDraftEnabled(section.isLlmDraftEnabled())
                .questionCount((int) questionRepository.countBySectionId(section.getId()))
                .build();
    }
}
