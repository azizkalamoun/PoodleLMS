package com.enterprise.poodle.service;

import com.enterprise.poodle.dto.response.AnalyticsResponse;
import com.enterprise.poodle.entity.*;
import com.enterprise.poodle.exception.ResourceNotFoundException;
import com.enterprise.poodle.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final CourseRepository courseRepository;
    private final CourseDepartmentAssignmentRepository assignmentRepository;
    private final EmployeeCourseGradeRepository gradeRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeCourseProgressRepository progressRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeQCMAttemptRepository attemptRepository;
    private final QCMQuestionRepository questionRepository;
    private final CourseSectionRepository sectionRepository;

    @Transactional(readOnly = true)
    public AnalyticsResponse.CompletionRate getCourseCompletionRate(Long courseId) {
        Course course = courseRepository.findByIdAndDeletedFalse(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // Count total assigned employees
        List<CourseDepartmentAssignment> assignments = assignmentRepository.findByCourseId(courseId);
        long totalAssigned = 0;
        for (CourseDepartmentAssignment assignment : assignments) {
            totalAssigned += employeeRepository.findByDepartmentId(
                    assignment.getDepartment().getId()).size();
        }

        // Count employees who completed all sections
        long totalSections = sectionRepository.countByCourseId(courseId);
        long totalCompleted = 0;

        if (totalSections > 0 && totalAssigned > 0) {
            // Get all employees from assigned departments
            List<Long> deptIds = assignments.stream()
                    .map(a -> a.getDepartment().getId())
                    .collect(Collectors.toList());
            List<Employee> employees = employeeRepository.findByDepartmentIdIn(deptIds);

            for (Employee emp : employees) {
                long completed = progressRepository.countByEmployeeIdAndCourseIdAndStatus(
                        emp.getId(), courseId,
                        com.enterprise.poodle.enums.ProgressStatus.COMPLETED);
                if (completed >= totalSections) {
                    totalCompleted++;
                }
            }
        }

        double rate = totalAssigned > 0
                ? (double) totalCompleted / totalAssigned * 100 : 0;

        return AnalyticsResponse.CompletionRate.builder()
                .courseId(courseId)
                .courseTitle(course.getTitle())
                .totalAssigned(totalAssigned)
                .totalCompleted(totalCompleted)
                .completionRate(Math.round(rate * 100.0) / 100.0)
                .build();
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse.PassRate getCoursePassRate(Long courseId) {
        Course course = courseRepository.findByIdAndDeletedFalse(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        long totalAttempted = gradeRepository.countByCourseId(courseId);
        long totalPassed = gradeRepository.countPassedByCourseId(courseId);
        double passRate = totalAttempted > 0
                ? (double) totalPassed / totalAttempted * 100 : 0;

        return AnalyticsResponse.PassRate.builder()
                .courseId(courseId)
                .courseTitle(course.getTitle())
                .totalAttempted(totalAttempted)
                .totalPassed(totalPassed)
                .passRate(Math.round(passRate * 100.0) / 100.0)
                .build();
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse.DepartmentAverageScore getDepartmentAverageScore(Long departmentId) {
        Department department = departmentRepository.findByIdAndDeletedFalse(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department", "id", departmentId));

        Double avgScore = gradeRepository.findAverageScoreByDepartmentId(departmentId);
        long totalGraded = gradeRepository.countByDepartmentId(departmentId);

        return AnalyticsResponse.DepartmentAverageScore.builder()
                .departmentId(departmentId)
                .departmentName(department.getName())
                .averageScore(avgScore != null ? Math.round(avgScore * 100.0) / 100.0 : 0)
                .totalGraded(totalGraded)
                .build();
    }

    @Transactional(readOnly = true)
    public List<AnalyticsResponse.OverdueEmployee> getOverdueEmployees(Long courseId) {
        courseRepository.findByIdAndDeletedFalse(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        List<CourseDepartmentAssignment> overdueAssignments =
                assignmentRepository.findOverdueAssignments(courseId);

        return overdueAssignments.stream()
                .flatMap(assignment -> {
                    List<Employee> deptEmployees = employeeRepository
                            .findByDepartmentId(assignment.getDepartment().getId());

                    return deptEmployees.stream()
                            .filter(emp -> {
                                // Check if employee has NOT passed the course
                                return !gradeRepository.existsByEmployeeIdAndCourseIdAndPassedTrue(
                                        emp.getId(), courseId);
                            })
                            .map(emp -> AnalyticsResponse.OverdueEmployee.builder()
                                    .employeeId(emp.getId())
                                    .employeeName(emp.getFirstName() + " " + emp.getLastName())
                                    .email(emp.getEmail())
                                    .departmentName(assignment.getDepartment().getName())
                                    .deadlineDate(assignment.getDeadlineDate().toString())
                                    .build());
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnalyticsResponse.MostFailedQuestion> getMostFailedQuestions(Long courseId) {
        courseRepository.findByIdAndDeletedFalse(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // Get all QCM sections for the course
        List<CourseSection> sections = sectionRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        List<Long> qcmSectionIds = sections.stream()
                .filter(s -> s.getContentType() == com.enterprise.poodle.enums.ContentType.QCM)
                .map(CourseSection::getId)
                .collect(Collectors.toList());

        if (qcmSectionIds.isEmpty()) {
            return List.of();
        }

        // Get all questions for these sections and compute fail rates per section
        return qcmSectionIds.stream()
                .flatMap(sId -> {
                    CourseSection sec = sectionRepository.findById(sId).orElse(null);
                    int passingScore = sec != null && sec.getCourse() != null
                            ? sec.getCourse().getPassingScore() : 70;
                    long totalAttempts = attemptRepository.countBySectionId(sId);
                    long failedAttempts = attemptRepository.countFailedBySectionId(sId, passingScore);
                    double failRate = totalAttempts > 0
                            ? (double) failedAttempts / totalAttempts * 100 : 0;

                    return questionRepository.findBySectionId(sId).stream()
                            .map(q -> AnalyticsResponse.MostFailedQuestion.builder()
                                    .questionId(q.getId())
                                    .questionText(q.getQuestionText())
                                    .totalAttempts(totalAttempts)
                                    .incorrectCount(failedAttempts)
                                    .failRate(Math.round(failRate * 100.0) / 100.0)
                                    .build());
                })
                .sorted((a, b) -> Double.compare(b.getFailRate(), a.getFailRate()))
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Get overall analytics overview for dashboard
     */
    @Transactional(readOnly = true)
    public AnalyticsResponse.OverviewData getAnalyticsOverview() {
        // Count active employees and courses using proper repository methods
        List<Department> depts = departmentRepository.findAllByDeletedFalse();
        List<Long> deptIds = depts.stream().map(Department::getId).collect(Collectors.toList());
        
        long totalEmployees = deptIds.isEmpty() ? 0 :
                employeeRepository.findByDepartmentIdIn(deptIds).size();
        
        long totalCourses = courseRepository.findAllByDeletedFalse(PageRequest.of(0, 1000)).getTotalElements();
        long totalEnrollments = assignmentRepository.count();

        // Calculate overall completion and pass rates
        double avgCompletionRate = 0;
        double avgPassRate = 0;
        double avgScore = 0;

        if (totalCourses > 0) {
            List<Course> courses = courseRepository.findAllByDeletedFalse(PageRequest.of(0, 1000)).getContent();
            
            for (Course course : courses) {
                AnalyticsResponse.CompletionRate completion = getCourseCompletionRate(course.getId());
                avgCompletionRate += completion.getCompletionRate();

                AnalyticsResponse.PassRate passRate = getCoursePassRate(course.getId());
                avgPassRate += passRate.getPassRate();

                Double courseAvgScore = gradeRepository.findAverageScoreByDepartmentId(course.getId());
                avgScore += (courseAvgScore != null ? courseAvgScore : 0);
            }
            avgCompletionRate /= totalCourses;
            avgPassRate /= totalCourses;
            avgScore /= totalCourses;
        }

        return AnalyticsResponse.OverviewData.builder()
                .totalEmployees(totalEmployees)
                .totalCourses(totalCourses)
                .totalEnrollments(totalEnrollments)
                .completionRate(Math.round(avgCompletionRate * 100.0) / 100.0)
                .passRate(Math.round(avgPassRate * 100.0) / 100.0)
                .averageScore(Math.round(avgScore * 100.0) / 100.0)
                .build();
    }

    /**
     * Get analytics for all courses
     */
    @Transactional(readOnly = true)
    public List<AnalyticsResponse.CourseAnalyticsData> getAllCoursesAnalytics() {
        return courseRepository.findAllByDeletedFalse(PageRequest.of(0, 1000)).getContent().stream()
                .map(course -> {
                    AnalyticsResponse.CompletionRate completion = getCourseCompletionRate(course.getId());
                    AnalyticsResponse.PassRate passRate = getCoursePassRate(course.getId());
                    Double avgScore = gradeRepository.findAverageScoreByDepartmentId(course.getId());

                    long enrollmentCount = assignmentRepository.findByCourseId(course.getId()).size();

                    return AnalyticsResponse.CourseAnalyticsData.builder()
                            .courseId(course.getId())
                            .courseTitle(course.getTitle())
                            .enrollmentCount(enrollmentCount)
                            .completionRate(completion.getCompletionRate())
                            .passRate(passRate.getPassRate())
                            .averageScore(Math.round((avgScore != null ? avgScore : 0) * 100.0) / 100.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Get all overdue enrollments across all courses
     */
    @Transactional(readOnly = true)
    public List<AnalyticsResponse.OverdueEnrollmentData> getAllOverdueEnrollments() {
        return assignmentRepository.findAll().stream()
                .flatMap(assignment -> {
                    List<Employee> employees = employeeRepository.findByDepartmentId(assignment.getDepartment().getId());
                    return employees.stream()
                            .filter(emp -> assignment.getDeadlineDate() != null && assignment.getDeadlineDate().isBefore(LocalDate.now()))
                            .map(emp -> AnalyticsResponse.OverdueEnrollmentData.builder()
                                    .enrollmentId(assignment.getId())
                                    .employeeName(emp.getFirstName() + " " + emp.getLastName())
                                    .courseTitle(assignment.getCourse().getTitle())
                                    .deadline(assignment.getDeadlineDate().toString())
                                    .departmentName(assignment.getDepartment().getName())
                                    .build());
                })
                .collect(Collectors.toList());
    }

    /**
     * Get most failed questions across all courses
     */
    @Transactional(readOnly = true)
    public List<AnalyticsResponse.FailedQuestionData> getMostFailedQuestionsGlobally() {
        return questionRepository.findAll().stream()
                .map(q -> {
                    CourseSection section = q.getSection();
                    long totalAttempts = attemptRepository.countBySectionId(section.getId());
                    int passingScore = section != null && section.getCourse() != null
                            ? section.getCourse().getPassingScore() : 70;
                    long failedAttempts = attemptRepository.countFailedBySectionId(section.getId(), passingScore);
                    double failRate = totalAttempts > 0 ? (double) failedAttempts / totalAttempts * 100 : 0;

                    return AnalyticsResponse.FailedQuestionData.builder()
                            .questionId(q.getId())
                            .questionText(q.getQuestionText())
                            .courseTitle(section.getCourse().getTitle())
                            .sectionTitle(section.getTitle())
                            .failureRate(Math.round(failRate * 100.0) / 100.0)
                            .totalAttempts(totalAttempts)
                            .build();
                })
                .sorted((a, b) -> Double.compare(b.getFailureRate(), a.getFailureRate()))
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Get analytics for all departments
     */
    @Transactional(readOnly = true)
    public List<AnalyticsResponse.DepartmentAnalyticsData> getAllDepartmentsAnalytics() {
        return departmentRepository.findAllByDeletedFalse().stream()
                .map(dept -> {
                    AnalyticsResponse.DepartmentAverageScore deptScore = getDepartmentAverageScore(dept.getId());
                    long employeeCount = employeeRepository.findByDepartmentId(dept.getId()).size();

                    // Calculate department completion and pass rates
                    double avgCompletionRate = 0;
                    double avgPassRate = 0;
                    long assignmentCount = assignmentRepository.findByDepartmentId(dept.getId()).size();

                    if (assignmentCount > 0) {
                        for (CourseDepartmentAssignment assignment : assignmentRepository.findByDepartmentId(dept.getId())) {
                            AnalyticsResponse.CompletionRate completion = getCourseCompletionRate(assignment.getCourse().getId());
                            AnalyticsResponse.PassRate passRate = getCoursePassRate(assignment.getCourse().getId());
                            avgCompletionRate += completion.getCompletionRate();
                            avgPassRate += passRate.getPassRate();
                        }
                        avgCompletionRate /= assignmentCount;
                        avgPassRate /= assignmentCount;
                    }

                    return AnalyticsResponse.DepartmentAnalyticsData.builder()
                            .departmentId(dept.getId())
                            .departmentName(dept.getName())
                            .completionRate(Math.round(avgCompletionRate * 100.0) / 100.0)
                            .passRate(Math.round(avgPassRate * 100.0) / 100.0)
                            .averageScore(deptScore.getAverageScore())
                            .employeeCount(employeeCount)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
