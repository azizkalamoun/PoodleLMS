package com.enterprise.poodle;

import com.enterprise.poodle.dto.request.LoginRequest;
import com.enterprise.poodle.dto.response.AuthResponse;
import com.enterprise.poodle.entity.*;
import com.enterprise.poodle.enums.*;
import com.enterprise.poodle.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected PasswordEncoder passwordEncoder;

    @Autowired protected EmployeeRepository employeeRepository;
    @Autowired protected DepartmentRepository departmentRepository;
    @Autowired protected CourseRepository courseRepository;
    @Autowired protected CourseSectionRepository sectionRepository;
    @Autowired protected CoursePrerequisiteRepository prerequisiteRepository;
    @Autowired protected CourseDepartmentAssignmentRepository assignmentRepository;
    @Autowired protected QCMQuestionRepository questionRepository;
    @Autowired protected EmployeeCourseProgressRepository progressRepository;
    @Autowired protected EmployeeQCMAttemptRepository attemptRepository;
    @Autowired protected EmployeeCourseGradeRepository gradeRepository;
    @Autowired protected CertificateRepository certificateRepository;
    @Autowired protected AuditLogRepository auditLogRepository;
    @Autowired protected EmployeeQCMAttemptAnswerRepository employeeQCMAttemptAnswerRepository;

    // Shared test data references
    protected Department engineering;
    protected Department backend;
    protected Department frontend;
    protected Department hr;

    protected Employee adminUser;
    protected Employee employeeUser;
    protected Employee employeeUser2;

    protected Course publishedCourse;
    protected Course draftCourse;
    protected CourseSection videoSection;
    protected CourseSection practiceQcmSection;
    protected CourseSection finalQcmSection;
    protected QCMQuestion question1;
    protected QCMQuestion question2;
    protected QCMQuestion question3;
    protected QCMQuestion question4;

    protected String adminToken;
    protected String employeeToken;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        seedTestData();
        obtainTokens();
    }

    @AfterEach
    protected void tearDown() {
        cleanDatabase();
    }

    protected void cleanDatabase() {
        // Delete entities with dependencies first
        auditLogRepository.deleteAll();
        certificateRepository.deleteAll();
        gradeRepository.deleteAll();
        employeeQCMAttemptAnswerRepository.deleteAll();
        attemptRepository.deleteAll();
        progressRepository.deleteAll();
        questionRepository.deleteAll();
        assignmentRepository.deleteAll();
        prerequisiteRepository.deleteAll();
        sectionRepository.deleteAll();
        courseRepository.deleteAll();
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
    }

    private void seedTestData() {
        // Departments
        engineering = departmentRepository.save(Department.builder().name("Engineering").build());
        backend = departmentRepository.save(Department.builder()
                .name("Backend").parentDepartment(engineering).build());
        frontend = departmentRepository.save(Department.builder()
                .name("Frontend").parentDepartment(engineering).build());
        hr = departmentRepository.save(Department.builder().name("HR").build());

        // Employees
        adminUser = employeeRepository.save(Employee.builder()
                .firstName("Admin").lastName("User")
                .email("admin@test.com")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.ROLE_ADMIN).build());

        employeeUser = employeeRepository.save(Employee.builder()
                .firstName("John").lastName("Doe")
                .email("employee@test.com")
                .password(passwordEncoder.encode("Employee@123"))
                .role(Role.ROLE_EMPLOYEE).department(backend).build());

        employeeUser2 = employeeRepository.save(Employee.builder()
                .firstName("Jane").lastName("Smith")
                .email("employee2@test.com")
                .password(passwordEncoder.encode("Employee@123"))
                .role(Role.ROLE_EMPLOYEE).department(frontend).build());

        // Courses
        publishedCourse = courseRepository.save(Course.builder()
                .title("Test Course").description("A published test course")
                .status(CourseStatus.PUBLISHED).passingScore(70).build());

        draftCourse = courseRepository.save(Course.builder()
                .title("Draft Course").description("A draft course")
                .status(CourseStatus.DRAFT).passingScore(70).build());

        // Sections
        videoSection = sectionRepository.save(CourseSection.builder()
                .course(publishedCourse).title("Intro Video")
                .contentType(ContentType.VIDEO).contentUrl("https://example.com/video.mp4")
                .orderIndex(0).build());

        practiceQcmSection = sectionRepository.save(CourseSection.builder()
                .course(publishedCourse).title("Practice Quiz")
                .contentType(ContentType.QCM).qcmType(QcmType.PRACTICE)
                .orderIndex(1).maxAttempts(5).build());

        finalQcmSection = sectionRepository.save(CourseSection.builder()
                .course(publishedCourse).title("Final Exam")
                .contentType(ContentType.QCM).qcmType(QcmType.FINAL)
                .orderIndex(2).maxAttempts(3).build());

        // QCM Questions
        question1 = questionRepository.save(QCMQuestion.builder()
                .section(finalQcmSection).questionText("What is 1 + 1?")
                .optionA("1").optionB("2").optionC("3").optionD("4")
                .correctOption("B").build());

        question2 = questionRepository.save(QCMQuestion.builder()
                .section(finalQcmSection).questionText("What is 2 + 2?")
                .optionA("2").optionB("3").optionC("4").optionD("5")
                .correctOption("C").build());

        question3 = questionRepository.save(QCMQuestion.builder()
                .section(practiceQcmSection).questionText("What is the capital of France?")
                .optionA("Berlin").optionB("Madrid").optionC("Paris").optionD("Rome")
                .correctOption("C").build());

        question4 = questionRepository.save(QCMQuestion.builder()
                .section(practiceQcmSection).questionText("What is 3 * 3?")
                .optionA("6").optionB("9").optionC("12").optionD("3")
                .correctOption("B").build());

        // Assign published course to backend department
        assignmentRepository.save(CourseDepartmentAssignment.builder()
                .course(publishedCourse).department(backend)
                .deadlineDate(LocalDate.now().plusMonths(2)).build());
    }

    private void obtainTokens() throws Exception {
        adminToken = login("admin@test.com", "Admin@123");
        employeeToken = login("employee@test.com", "Employee@123");
    }

    protected String login(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseBody, AuthResponse.class);
        return authResponse.getToken();
    }

    protected String asJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
