package com.enterprise.poodle.service;

import com.enterprise.poodle.dto.response.CourseProgressResponse;
import com.enterprise.poodle.entity.*;
import com.enterprise.poodle.enums.ContentType;
import com.enterprise.poodle.enums.CourseStatus;
import com.enterprise.poodle.enums.ProgressStatus;
import com.enterprise.poodle.exception.BusinessException;
import com.enterprise.poodle.exception.ResourceNotFoundException;
import com.enterprise.poodle.repository.*;
import com.enterprise.poodle.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final EmployeeCourseProgressRepository progressRepository;
    private final CourseRepository courseRepository;
    private final CourseSectionRepository sectionRepository;
    private final EmployeeCourseGradeRepository gradeRepository;
    private final CourseDepartmentAssignmentRepository assignmentRepository;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public CourseProgressResponse getCourseProgress(Long courseId) {
        Employee employee = securityUtils.getCurrentEmployee();
        Course course = courseRepository.findByIdAndDeletedFalse(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new BusinessException("Cannot access draft or archived courses");
        }

        List<CourseSection> sections = sectionRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        List<EmployeeCourseProgress> progressList =
                progressRepository.findByEmployeeIdAndCourseId(employee.getId(), courseId);

        Map<Long, EmployeeCourseProgress> progressMap = progressList.stream()
                .collect(Collectors.toMap(
                        p -> p.getSection().getId(), p -> p));

        long completedCount = progressList.stream()
                .filter(p -> p.getStatus() == ProgressStatus.COMPLETED)
                .count();

        List<CourseProgressResponse.SectionProgressResponse> sectionProgress = sections.stream()
                .map(section -> {
                    EmployeeCourseProgress p = progressMap.get(section.getId());
                    return CourseProgressResponse.SectionProgressResponse.builder()
                            .sectionId(section.getId())
                            .sectionTitle(section.getTitle())
                            .status(p != null ? p.getStatus() : ProgressStatus.NOT_STARTED)
                            .completedAt(p != null ? p.getCompletedAt() : null)
                            .build();
                })
                .collect(Collectors.toList());

        // Get grade if exists
        Integer finalScore = null;
        Boolean passed = null;
        var grade = gradeRepository.findByEmployeeIdAndCourseId(employee.getId(), courseId);
        if (grade.isPresent()) {
            finalScore = grade.get().getFinalScore();
            passed = grade.get().isPassed();
        }

        double completionPercentage = sections.isEmpty() ? 0 :
                (double) completedCount / sections.size() * 100;

        return CourseProgressResponse.builder()
                .courseId(courseId)
                .courseTitle(course.getTitle())
                .totalSections(sections.size())
                .completedSections((int) completedCount)
                .completionPercentage(Math.round(completionPercentage * 100.0) / 100.0)
                .finalScore(finalScore)
                .passed(passed)
                .sectionProgress(sectionProgress)
                .build();
    }

    @Transactional
    public void markSectionCompleted(Long sectionId) {
        Employee employee = securityUtils.getCurrentEmployee();

        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));

        // Only allow marking non-QCM sections (QCM progress is handled by QCMService)
        if (section.getContentType() == ContentType.QCM) {
            throw new BusinessException(
                    "QCM sections are automatically completed when you submit an attempt");
        }

        Course course = section.getCourse();
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new BusinessException("Cannot track progress on non-published courses");
        }

        // Check if employee's department is assigned to this course
        if (employee.getDepartment() == null ||
                !assignmentRepository.existsByCourseIdAndDepartmentId(
                        course.getId(), employee.getDepartment().getId())) {
            throw new BusinessException("Course is not assigned to your department");
        }

        EmployeeCourseProgress progress = progressRepository
                .findByEmployeeIdAndSectionId(employee.getId(), sectionId)
                .orElse(EmployeeCourseProgress.builder()
                        .employee(employee)
                        .course(course)
                        .section(section)
                        .status(ProgressStatus.NOT_STARTED)
                        .build());

        if (progress.getStatus() == ProgressStatus.COMPLETED) {
            throw new BusinessException("Section is already completed");
        }

        progress.setStatus(ProgressStatus.COMPLETED);
        progress.setCompletedAt(LocalDateTime.now());
        progressRepository.save(progress);
    }
}
