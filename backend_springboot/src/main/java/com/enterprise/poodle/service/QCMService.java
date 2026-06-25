package com.enterprise.poodle.service;

import com.enterprise.poodle.dto.request.QCMAttemptRequest;
import com.enterprise.poodle.dto.request.QCMQuestionRequest;
import com.enterprise.poodle.dto.response.AttemptCountResponse;
import com.enterprise.poodle.dto.response.QCMAttemptResponse;
import com.enterprise.poodle.dto.response.QCMQuestionResponse;
import com.enterprise.poodle.entity.*;
import com.enterprise.poodle.enums.*;
import com.enterprise.poodle.exception.BusinessException;
import com.enterprise.poodle.exception.ResourceNotFoundException;
import com.enterprise.poodle.mapper.QCMQuestionMapper;
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
public class QCMService {

    private final QCMQuestionRepository questionRepository;
    private final CourseSectionRepository sectionRepository;
    private final EmployeeQCMAttemptRepository attemptRepository;
    private final com.enterprise.poodle.repository.EmployeeQCMAttemptAnswerRepository answerRepository;
    private final EmployeeCourseGradeRepository gradeRepository;
    private final EmployeeCourseProgressRepository progressRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final CertificateRepository certificateRepository;
    private final SecurityUtils securityUtils;
    private final AuditLogService auditLogService;
    private final CertificateService certificateService;
    private final QCMQuestionMapper qcmQuestionMapper;

    @Transactional
    public QCMQuestionResponse createQuestion(Long sectionId, QCMQuestionRequest request) {
        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));

        if (section.getContentType() != ContentType.QCM) {
            throw new BusinessException("Questions can only be added to QCM sections");
        }

        QCMQuestion question = QCMQuestion.builder()
                .section(section)
                .questionText(request.getQuestionText())
                .optionA(request.getOptionA())
                .optionB(request.getOptionB())
                .optionC(request.getOptionC())
                .optionD(request.getOptionD())
                .correctOption(request.getCorrectOption())
                .llmGenerated(false)
                .build();

        QCMQuestion saved = questionRepository.save(question);

        auditLogService.logAction(ActionType.CREATE, "QCMQuestion", saved.getId(), null,
                Map.of("questionText", saved.getQuestionText(), "sectionId", sectionId));

        return mapToResponse(saved, true);
    }

    @Transactional(readOnly = true)
    public List<QCMQuestionResponse> getQuestions(Long sectionId) {
        sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));

        boolean isAdmin = securityUtils.isAdmin();

        return questionRepository.findBySectionId(sectionId).stream()
                .map(q -> mapToResponse(q, isAdmin))
                .collect(Collectors.toList());
    }

    @Transactional
    public QCMQuestionResponse updateQuestion(Long sectionId, Long questionId, QCMQuestionRequest request) {
        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));

        QCMQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", questionId));

        if (!question.getSection().getId().equals(sectionId)) {
            throw new BusinessException("Question does not belong to this section");
        }

        question.setQuestionText(request.getQuestionText());
        question.setOptionA(request.getOptionA());
        question.setOptionB(request.getOptionB());
        question.setOptionC(request.getOptionC());
        question.setOptionD(request.getOptionD());
        question.setCorrectOption(request.getCorrectOption());

        QCMQuestion saved = questionRepository.save(question);

        auditLogService.logAction(ActionType.UPDATE, "QCMQuestion", saved.getId(), null,
                Map.of("questionText", saved.getQuestionText(), "sectionId", sectionId));

        return mapToResponse(saved, true);
    }

    @Transactional
    public void deleteQuestion(Long sectionId, Long questionId) {
        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));

        QCMQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", questionId));

        if (!question.getSection().getId().equals(sectionId)) {
            throw new BusinessException("Question does not belong to this section");
        }

        questionRepository.delete(question);

        auditLogService.logAction(ActionType.DELETE, "QCMQuestion", questionId, null, null);
    }

    @Transactional(readOnly = true)
    public AttemptCountResponse getMyAttemptCount(Long sectionId) {
        Employee employee = securityUtils.getCurrentEmployee();
        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));
        long used = attemptRepository.countByEmployeeIdAndSectionId(employee.getId(), sectionId);
        return AttemptCountResponse.builder()
                .attemptsUsed(used)
                .maxAttempts(section.getMaxAttempts())
                .exhausted(section.getMaxAttempts() != null && used >= section.getMaxAttempts())
                .build();
    }

    @Transactional
    public QCMAttemptResponse submitAttempt(Long sectionId, QCMAttemptRequest request) {
        Employee employee = securityUtils.getCurrentEmployee();
        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));

        if (section.getContentType() != ContentType.QCM) {
            throw new BusinessException("This section is not a QCM");
        }

        // Check max attempts
        if (section.getMaxAttempts() != null) {
            long currentAttempts = attemptRepository.countByEmployeeIdAndSectionId(
                    employee.getId(), sectionId);
            if (currentAttempts >= section.getMaxAttempts()) {
                throw new BusinessException("Maximum number of attempts reached (" +
                        section.getMaxAttempts() + ")");
            }
        }

        // Calculate score
        List<QCMQuestion> questions = questionRepository.findBySectionId(sectionId);
        if (questions.isEmpty()) {
            throw new BusinessException("No questions found in this section");
        }

        int correctCount = 0;
        // Build per-question answer results for frontend feedback
        List<com.enterprise.poodle.dto.response.QCMAttemptAnswerResult> answerResults = new java.util.ArrayList<>();
        for (QCMQuestion question : questions) {
            String selectedLetter = request.getAnswers().get(question.getId());
            String correctLetter = question.getCorrectOption();

            int selectedIndex = -1;
            if (selectedLetter != null && !selectedLetter.isEmpty()) {
                selectedIndex = selectedLetter.charAt(0) - 'A';
            }
            int correctIndex = (correctLetter != null && !correctLetter.isEmpty()) ? (correctLetter.charAt(0) - 'A') : -1;

            boolean correct = selectedLetter != null && selectedLetter.equals(correctLetter);
            if (correct) correctCount++;

            answerResults.add(com.enterprise.poodle.dto.response.QCMAttemptAnswerResult.builder()
                    .questionId(question.getId())
                    .selectedAnswerIndex(selectedIndex)
                    .correctAnswerIndex(correctIndex)
                    .correct(correct)
                    .build());
        }

        int score = (int) Math.round((double) correctCount / questions.size() * 100);

        // Determine attempt number
        Integer maxAttempt = attemptRepository.findMaxAttemptNumber(
                employee.getId(), sectionId);
        int attemptNumber = (maxAttempt != null ? maxAttempt : 0) + 1;

        // Save attempt
        EmployeeQCMAttempt attempt = EmployeeQCMAttempt.builder()
                .employee(employee)
                .section(section)
                .attemptNumber(attemptNumber)
                .score(score)
                .takenAt(LocalDateTime.now())
                .build();

        attempt = attemptRepository.save(attempt);


    java.util.List<com.enterprise.poodle.entity.EmployeeQCMAttemptAnswer> answerEntities = new java.util.ArrayList<>();
    for (QCMQuestion question : questions) {
        String selectedLetter = request.getAnswers().get(question.getId());
        boolean correct = selectedLetter != null && selectedLetter.equals(question.getCorrectOption());
        com.enterprise.poodle.entity.EmployeeQCMAttemptAnswer ans = com.enterprise.poodle.entity.EmployeeQCMAttemptAnswer.builder()
            .attempt(attempt)
            .questionId(question.getId())
            .selectedOption(selectedLetter)
            .correct(correct)
            .build();
        answerEntities.add(ans);
    }
    answerRepository.saveAll(answerEntities);

        // Update progress
        updateProgress(employee, section);

        // If FINAL QCM, update grade and potentially issue certificate
        Course course = section.getCourse();
        boolean passed = false;
        if (section.getQcmType() == QcmType.FINAL) {
            passed = score >= course.getPassingScore();
            updateGrade(employee, course, score, passed);

            if (passed) {
                certificateService.generateCertificateIfEligible(employee, course);
            }
        }

        long attemptsUsedNow = attemptNumber;
        Integer sectionMax = section.getMaxAttempts();
        boolean nowExhausted = sectionMax != null && attemptsUsedNow >= sectionMax;

    return QCMAttemptResponse.builder()
        .id(attempt.getId())
        .sectionId(sectionId)
        .attemptNumber(attempt.getAttemptNumber())
        .score(attempt.getScore())
        .totalQuestions(questions.size())
        .correctAnswers(correctCount)
        .attemptsUsed(attemptsUsedNow)
        .maxAttempts(sectionMax)
        .exhausted(nowExhausted)
        .passed(section.getQcmType() == QcmType.FINAL ? passed : score >= 50)
        .takenAt(attempt.getTakenAt())
        .answers(answerResults)
        .build();
    }

    private void updateProgress(Employee employee, CourseSection section) {
        EmployeeCourseProgress progress = progressRepository
                .findByEmployeeIdAndSectionId(employee.getId(), section.getId())
                .orElse(EmployeeCourseProgress.builder()
                        .employee(employee)
                        .course(section.getCourse())
                        .section(section)
                        .status(ProgressStatus.NOT_STARTED)
                        .build());

        progress.setStatus(ProgressStatus.COMPLETED);
        progress.setCompletedAt(LocalDateTime.now());
        progressRepository.save(progress);
    }

    private void updateGrade(Employee employee, Course course, int score, boolean passed) {
        EmployeeCourseGrade grade = gradeRepository
                .findByEmployeeIdAndCourseId(employee.getId(), course.getId())
                .orElse(EmployeeCourseGrade.builder()
                        .employee(employee)
                        .course(course)
                        .finalScore(0)
                        .passed(false)
                        .build());

        grade.setFinalScore(score);
        grade.setPassed(passed);
        grade.setUpdatedAt(LocalDateTime.now());
        gradeRepository.save(grade);
    }

    private QCMQuestionResponse mapToResponse(QCMQuestion question, boolean includeCorrect) {
        return includeCorrect
                ? qcmQuestionMapper.toResponseWithAnswer(question)
                : qcmQuestionMapper.toResponse(question);
    }
}
