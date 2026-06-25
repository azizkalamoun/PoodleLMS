package com.enterprise.poodle.service;

import com.enterprise.poodle.dto.request.QCMAttemptFromEnrollmentRequest;
import com.enterprise.poodle.dto.request.QCMAttemptRequest;
import com.enterprise.poodle.dto.response.EnrollmentResponse;
import com.enterprise.poodle.dto.response.QCMAttemptResponse;
import com.enterprise.poodle.entity.Course;
import com.enterprise.poodle.entity.CourseDepartmentAssignment;
import com.enterprise.poodle.entity.CourseSection;
import com.enterprise.poodle.entity.Employee;
import com.enterprise.poodle.entity.EmployeeCourseGrade;
import com.enterprise.poodle.exception.BusinessException;
import com.enterprise.poodle.repository.CourseDepartmentAssignmentRepository;
import com.enterprise.poodle.repository.CourseSectionRepository;
import com.enterprise.poodle.repository.EmployeeCourseGradeRepository;
import com.enterprise.poodle.repository.EmployeeQCMAttemptRepository;
import com.enterprise.poodle.repository.EmployeeRepository;
import com.enterprise.poodle.security.SecurityUtils;
import com.enterprise.poodle.enums.QcmType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final CourseDepartmentAssignmentRepository assignmentRepository;
    private final EmployeeCourseGradeRepository gradeRepository;
    private final CourseSectionRepository sectionRepository;
    private final EmployeeQCMAttemptRepository attemptRepository;
    private final EmployeeRepository employeeRepository;
    private final QCMService qcmService;
    private final SecurityUtils securityUtils;

    /**
     * Get all enrollments for the current employee
     * An enrollment is created when a course is assigned to an employee's department
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getMyEnrollments() {
        Employee employee = securityUtils.getCurrentEmployee();
        
        if (employee.getDepartment() == null) {
            log.warn("Employee {} has no department assigned", employee.getId());
            return List.of();
        }

        // Get all courses assigned to employee's department
        List<CourseDepartmentAssignment> assignments = 
                assignmentRepository.findByDepartmentId(employee.getDepartment().getId());

        return assignments.stream()
                .map(assignment -> mapToEnrollmentResponse(assignment, employee))
                .collect(Collectors.toList());
    }

    /**
     * Get all enrollments for a specific employee (used by admins to view employee progress)
     * 
     * @param employeeId The ID of the employee
     * @return List of enrollments for the specified employee
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByEmployeeId(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException("Employee not found"));
        
        if (employee.getDepartment() == null) {
            log.warn("Employee {} has no department assigned", employeeId);
            return List.of();
        }

        // Get all courses assigned to the employee's department
        List<CourseDepartmentAssignment> assignments = 
                assignmentRepository.findByDepartmentId(employee.getDepartment().getId());

        return assignments.stream()
                .map(assignment -> mapToEnrollmentResponse(assignment, employee))
                .collect(Collectors.toList());
    }

    /**
     * Map CourseDepartmentAssignment and EmployeeCourseGrade to EnrollmentResponse
     */
    private EnrollmentResponse mapToEnrollmentResponse(CourseDepartmentAssignment assignment, Employee employee) {
        Course course = assignment.getCourse();
        
        // Get grade/score information if exists
        Optional<EmployeeCourseGrade> gradeOpt = gradeRepository.findByEmployeeIdAndCourseId(
                employee.getId(), course.getId());

        EnrollmentResponse.EnrollmentResponseBuilder builder = EnrollmentResponse.builder()
                .id(assignment.getId())
                .employeeId(employee.getId())
                .employeeName(employee.getFirstName() + " " + employee.getLastName())
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .deadline(assignment.getDeadlineDate());

        if (gradeOpt.isPresent()) {
            EmployeeCourseGrade grade = gradeOpt.get();
            builder.score(grade.getFinalScore() != null ? grade.getFinalScore().doubleValue() : null)
                    .attempts(1)
                    .updatedAt(grade.getUpdatedAt());
            
            // Determine enrollment status based on grade and deadline
            if (grade.isPassed()) {
                builder.status("PASSED");
            } else {
                // Grade exists but employee failed
                // Check if max attempts have been exhausted for the final QCM section
                boolean isFailed = isMaxAttemptsExhausted(course, employee);
                
                if (isFailed) {
                    builder.status("FAILED");
                } else if (assignment.getDeadlineDate() != null && 
                           assignment.getDeadlineDate().isBefore(LocalDate.now())) {
                    builder.status("OVERDUE");
                } else {
                    builder.status("IN_PROGRESS");
                }
            }
        } else {
            // No grade yet - enrollment in progress or not started
            if (assignment.getDeadlineDate() != null && 
                assignment.getDeadlineDate().isBefore(LocalDate.now())) {
                builder.status("OVERDUE");
            } else {
                builder.status("IN_PROGRESS");
            }
            builder.score(null)
                    .attempts(0);
        }

        return builder.build();
    }

    /**
     * Check if employee has exhausted max attempts for the final QCM section
     */
    private boolean isMaxAttemptsExhausted(Course course, Employee employee) {
        // Find the final QCM section
        CourseSection finalSection = sectionRepository.findByCourseIdAndQcmType(course.getId(), QcmType.FINAL)
                .orElse(null);
        
        if (finalSection == null || finalSection.getMaxAttempts() == null) {
            return false;
        }
        
        long attemptCount = attemptRepository.countByEmployeeIdAndSectionId(
                employee.getId(), finalSection.getId());
        
        return attemptCount >= finalSection.getMaxAttempts();
    }

    /**
     * Submit a practice QCM attempt for a section within an enrollment
     * Practice attempts don't affect final grades
     *
     * @param enrollmentId The enrollment ID (used for context/validation)
     * @param request The attempt request containing section ID and answers
     * @return The QCM attempt response with score and correctness
     */
    @Transactional
    public QCMAttemptResponse submitPracticeAttempt(
            Long enrollmentId,
            QCMAttemptFromEnrollmentRequest request) {
        
        // Validate enrollment exists and belongs to current user
        Employee employee = securityUtils.getCurrentEmployee();
        CourseDepartmentAssignment assignment = assignmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new BusinessException("Enrollment not found"));
        
        if (!assignment.getDepartment().getId().equals(employee.getDepartment().getId())) {
            throw new BusinessException("You do not have access to this enrollment");
        }

        // Validate section exists and belongs to the enrollment's course
        CourseSection section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new BusinessException("Section not found"));
        
        if (!section.getCourse().getId().equals(assignment.getCourse().getId())) {
            throw new BusinessException("Section does not belong to this enrollment's course");
        }

        // Convert the answers array format to Map format expected by QCMService
        QCMAttemptRequest qcmRequest = convertToQCMAttemptRequest(request);
        
        // Delegate to QCMService
        return qcmService.submitAttempt(request.getSectionId(), qcmRequest);
    }

    /**
     * Submit a final QCM attempt for a section within an enrollment
     * Final attempts affect final grades and may trigger certificate generation
     *
     * @param enrollmentId The enrollment ID (used for context/validation)
     * @param request The attempt request containing section ID and answers
     * @return The QCM attempt response with pass/fail determination
     */
    @Transactional
    public QCMAttemptResponse submitFinalAttempt(
            Long enrollmentId,
            QCMAttemptFromEnrollmentRequest request) {
        
        // Validate enrollment exists and belongs to current user
        Employee employee = securityUtils.getCurrentEmployee();
        CourseDepartmentAssignment assignment = assignmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new BusinessException("Enrollment not found"));
        
        if (!assignment.getDepartment().getId().equals(employee.getDepartment().getId())) {
            throw new BusinessException("You do not have access to this enrollment");
        }

        // Validate section exists and belongs to the enrollment's course
        CourseSection section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new BusinessException("Section not found"));
        
        if (!section.getCourse().getId().equals(assignment.getCourse().getId())) {
            throw new BusinessException("Section does not belong to this enrollment's course");
        }

        // Convert the answers array format to Map format expected by QCMService
        QCMAttemptRequest qcmRequest = convertToQCMAttemptRequest(request);
        
        // Delegate to QCMService
        return qcmService.submitAttempt(request.getSectionId(), qcmRequest);
    }

    /**
     * Convert from array-based QCMAttemptFromEnrollmentRequest to Map-based QCMAttemptRequest
     * Frontend sends selected answer index, but backend expects answer letter (A, B, C, D)
     * For now, we'll convert index to letter assumption: 0=A, 1=B, 2=C, 3=D
     *
     * @param request The enrollment request with array format
     * @return The QCM request with map format
     */
    private QCMAttemptRequest convertToQCMAttemptRequest(QCMAttemptFromEnrollmentRequest request) {
        Map<Long, String> answers = new HashMap<>();
        
        for (QCMAttemptFromEnrollmentRequest.QCMAnswerRequest answer : request.getAnswers()) {
            // Convert 0=A, 1=B, 2=C, 3=D
            String answerLetter = indexToLetter(answer.getSelectedAnswerIndex());
            answers.put(answer.getQuestionId(), answerLetter);
        }
        
        QCMAttemptRequest qcmRequest = new QCMAttemptRequest();
        qcmRequest.setAnswers(answers);
        return qcmRequest;
    }

    /**
     * Convert answer index to letter (0=A, 1=B, 2=C, 3=D)
     */
    private String indexToLetter(Integer index) {
        if (index == null || index < 0 || index > 3) {
            throw new BusinessException("Invalid or unanswered question detected. Please ensure all questions are answered before submitting. Got answer index: " + index);
        }
        return String.valueOf((char) ('A' + index));
    }
}
