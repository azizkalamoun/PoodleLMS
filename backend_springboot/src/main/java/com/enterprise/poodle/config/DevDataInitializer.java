package com.enterprise.poodle.config;

import com.enterprise.poodle.entity.*;
import com.enterprise.poodle.enums.*;
import com.enterprise.poodle.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DevDataInitializer {

    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDevData(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            CourseRepository courseRepository,
            CourseSectionRepository sectionRepository,
            CoursePrerequisiteRepository prerequisiteRepository,
            CourseDepartmentAssignmentRepository assignmentRepository,
            QCMQuestionRepository questionRepository,
            EmployeeCourseProgressRepository progressRepository,
            EmployeeQCMAttemptRepository attemptRepository,
            EmployeeCourseGradeRepository gradeRepository,
            CertificateRepository certificateRepository,
            AuditLogRepository auditLogRepository) {

        return args -> {
            if (employeeRepository.count() > 0) {
                log.info("Dev data already exists, skipping initialization");
                return;
            }

            log.info("=== Initializing dev seed data ===");

            // ── Departments ──────────────────────────────────────────
            Department engineering = departmentRepository.save(Department.builder()
                    .name("Engineering").build());
            Department backend = departmentRepository.save(Department.builder()
                    .name("Backend Engineering").parentDepartment(engineering).build());
            Department frontend = departmentRepository.save(Department.builder()
                    .name("Frontend Engineering").parentDepartment(engineering).build());
            Department hr = departmentRepository.save(Department.builder()
                    .name("Human Resources").build());
            Department marketing = departmentRepository.save(Department.builder()
                    .name("Marketing").build());
            log.info("Created 5 departments");

            // ── Employees ────────────────────────────────────────────
            Employee admin = employeeRepository.save(Employee.builder()
                    .firstName("System").lastName("Admin")
                    .email("admin@poodle.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ROLE_ADMIN).build());

            Employee john = employeeRepository.save(Employee.builder()
                    .firstName("John").lastName("Doe")
                    .email("john.doe@poodle.com")
                    .password(passwordEncoder.encode("Employee@123"))
                    .role(Role.ROLE_EMPLOYEE).department(backend).build());

            Employee jane = employeeRepository.save(Employee.builder()
                    .firstName("Jane").lastName("Smith")
                    .email("jane.smith@poodle.com")
                    .password(passwordEncoder.encode("Employee@123"))
                    .role(Role.ROLE_EMPLOYEE).department(frontend).build());

            Employee bob = employeeRepository.save(Employee.builder()
                    .firstName("Bob").lastName("Wilson")
                    .email("bob.wilson@poodle.com")
                    .password(passwordEncoder.encode("Employee@123"))
                    .role(Role.ROLE_EMPLOYEE).department(hr).build());

            Employee alice = employeeRepository.save(Employee.builder()
                    .firstName("Alice").lastName("Brown")
                    .email("alice.brown@poodle.com")
                    .password(passwordEncoder.encode("Employee@123"))
                    .role(Role.ROLE_EMPLOYEE).department(marketing).build());

            Employee charlie = employeeRepository.save(Employee.builder()
                    .firstName("Charlie").lastName("Davis")
                    .email("charlie.davis@poodle.com")
                    .password(passwordEncoder.encode("Employee@123"))
                    .role(Role.ROLE_EMPLOYEE).department(backend).build());
            log.info("Created 6 employees (1 admin + 5 employees)");

            // ── Courses ──────────────────────────────────────────────
            Course javaBasics = courseRepository.save(Course.builder()
                    .title("Java Fundamentals")
                    .description("Learn core Java programming concepts including OOP, collections, and streams.")
                    .status(CourseStatus.PUBLISHED).passingScore(70).build());

            Course springBoot = courseRepository.save(Course.builder()
                    .title("Spring Boot Mastery")
                    .description("Deep dive into Spring Boot, Spring Security, and Spring Data JPA.")
                    .status(CourseStatus.PUBLISHED).passingScore(75).build());

            Course reactCourse = courseRepository.save(Course.builder()
                    .title("React for Enterprise")
                    .description("Build scalable React applications with TypeScript and state management.")
                    .status(CourseStatus.PUBLISHED).passingScore(70).build());

            Course hrCompliance = courseRepository.save(Course.builder()
                    .title("HR Compliance Training")
                    .description("Annual compliance training covering workplace policies and regulations.")
                    .status(CourseStatus.PUBLISHED).passingScore(80).build());

            Course draftCourse = courseRepository.save(Course.builder()
                    .title("Advanced Microservices")
                    .description("Microservices architecture patterns, service mesh, and event-driven design.")
                    .status(CourseStatus.DRAFT).passingScore(70).build());

            Course archivedCourse = courseRepository.save(Course.builder()
                    .title("Legacy COBOL Training")
                    .description("This course has been archived.")
                    .status(CourseStatus.ARCHIVED).passingScore(60).build());
            log.info("Created 6 courses");

            // ── Prerequisites ────────────────────────────────────────
            prerequisiteRepository.save(CoursePrerequisite.builder()
                    .course(springBoot).prerequisiteCourse(javaBasics).build());
            log.info("Created 1 prerequisite (Spring Boot requires Java Fundamentals)");

            // ── Sections ─────────────────────────────────────────────
            // Java Fundamentals sections
            CourseSection javaIntro = sectionRepository.save(CourseSection.builder()
                    .course(javaBasics).title("Introduction to Java")
                    .contentType(ContentType.VIDEO).contentUrl("https://videos.poodle.com/java-intro.mp4")
                    .orderIndex(0).build());

            CourseSection javaOop = sectionRepository.save(CourseSection.builder()
                    .course(javaBasics).title("Object-Oriented Programming")
                    .contentType(ContentType.PDF).contentUrl("https://docs.poodle.com/java-oop.pdf")
                    .orderIndex(1).build());

            CourseSection javaCollections = sectionRepository.save(CourseSection.builder()
                    .course(javaBasics).title("Collections Framework")
                    .contentType(ContentType.TEXT).contentUrl("https://docs.poodle.com/java-collections")
                    .orderIndex(2).llmDraftEnabled(true).build());

            CourseSection javaPracticeQcm = sectionRepository.save(CourseSection.builder()
                    .course(javaBasics).title("Java Practice Quiz")
                    .contentType(ContentType.QCM).qcmType(QcmType.PRACTICE)
                    .orderIndex(3).maxAttempts(5).build());

            CourseSection javaFinalQcm = sectionRepository.save(CourseSection.builder()
                    .course(javaBasics).title("Java Final Exam")
                    .contentType(ContentType.QCM).qcmType(QcmType.FINAL)
                    .orderIndex(4).maxAttempts(3).build());

            // Spring Boot sections
            CourseSection sbIntro = sectionRepository.save(CourseSection.builder()
                    .course(springBoot).title("Getting Started with Spring Boot")
                    .contentType(ContentType.VIDEO).contentUrl("https://videos.poodle.com/sb-intro.mp4")
                    .orderIndex(0).build());

            CourseSection sbSecurity = sectionRepository.save(CourseSection.builder()
                    .course(springBoot).title("Spring Security & JWT")
                    .contentType(ContentType.PDF).contentUrl("https://docs.poodle.com/sb-security.pdf")
                    .orderIndex(1).build());

            CourseSection sbFinalQcm = sectionRepository.save(CourseSection.builder()
                    .course(springBoot).title("Spring Boot Final Exam")
                    .contentType(ContentType.QCM).qcmType(QcmType.FINAL)
                    .orderIndex(2).maxAttempts(3).build());

            // React course sections
            CourseSection reactIntro = sectionRepository.save(CourseSection.builder()
                    .course(reactCourse).title("React Fundamentals")
                    .contentType(ContentType.VIDEO).contentUrl("https://videos.poodle.com/react-intro.mp4")
                    .orderIndex(0).build());

            CourseSection reactFinalQcm = sectionRepository.save(CourseSection.builder()
                    .course(reactCourse).title("React Final Exam")
                    .contentType(ContentType.QCM).qcmType(QcmType.FINAL)
                    .orderIndex(1).maxAttempts(2).build());

            // HR Compliance sections
            CourseSection hrIntro = sectionRepository.save(CourseSection.builder()
                    .course(hrCompliance).title("Workplace Policies Overview")
                    .contentType(ContentType.PDF).contentUrl("https://docs.poodle.com/hr-policies.pdf")
                    .orderIndex(0).build());

            CourseSection hrFinalQcm = sectionRepository.save(CourseSection.builder()
                    .course(hrCompliance).title("Compliance Assessment")
                    .contentType(ContentType.QCM).qcmType(QcmType.FINAL)
                    .orderIndex(1).maxAttempts(2).build());
            log.info("Created 12 course sections");

            // ── QCM Questions ────────────────────────────────────────
            // Java Practice Quiz questions
            questionRepository.saveAll(List.of(
                    QCMQuestion.builder().section(javaPracticeQcm)
                            .questionText("Which keyword is used to create a new instance in Java?")
                            .optionA("new").optionB("create").optionC("instance").optionD("make")
                            .correctOption("A").build(),
                    QCMQuestion.builder().section(javaPracticeQcm)
                            .questionText("What is the default value of an int in Java?")
                            .optionA("null").optionB("-1").optionC("0").optionD("undefined")
                            .correctOption("C").build(),
                    QCMQuestion.builder().section(javaPracticeQcm)
                            .questionText("Which collection guarantees insertion order?")
                            .optionA("HashSet").optionB("TreeSet").optionC("LinkedHashSet").optionD("HashMap")
                            .correctOption("C").build()
            ));

            // Java Final Exam questions
            questionRepository.saveAll(List.of(
                    QCMQuestion.builder().section(javaFinalQcm)
                            .questionText("What does JVM stand for?")
                            .optionA("Java Virtual Machine").optionB("Java Variable Method")
                            .optionC("Java Verified Module").optionD("Java Version Manager")
                            .correctOption("A").build(),
                    QCMQuestion.builder().section(javaFinalQcm)
                            .questionText("Which access modifier makes a member visible only within its class?")
                            .optionA("public").optionB("protected").optionC("default").optionD("private")
                            .correctOption("D").build(),
                    QCMQuestion.builder().section(javaFinalQcm)
                            .questionText("What is the parent class of all classes in Java?")
                            .optionA("Main").optionB("Object").optionC("Class").optionD("Base")
                            .correctOption("B").build(),
                    QCMQuestion.builder().section(javaFinalQcm)
                            .questionText("Which keyword prevents a class from being subclassed?")
                            .optionA("static").optionB("abstract").optionC("final").optionD("sealed")
                            .correctOption("C").build()
            ));

            // Spring Boot Final questions
            questionRepository.saveAll(List.of(
                    QCMQuestion.builder().section(sbFinalQcm)
                            .questionText("What annotation marks the main Spring Boot class?")
                            .optionA("@SpringApp").optionB("@SpringBootApplication")
                            .optionC("@EnableSpring").optionD("@SpringMain")
                            .correctOption("B").build(),
                    QCMQuestion.builder().section(sbFinalQcm)
                            .questionText("Which starter includes an embedded Tomcat server?")
                            .optionA("spring-boot-starter-data").optionB("spring-boot-starter-web")
                            .optionC("spring-boot-starter-tomcat").optionD("spring-boot-starter-server")
                            .correctOption("B").build()
            ));

            // React Final questions
            questionRepository.saveAll(List.of(
                    QCMQuestion.builder().section(reactFinalQcm)
                            .questionText("What hook manages state in functional components?")
                            .optionA("useEffect").optionB("useState").optionC("useContext").optionD("useReducer")
                            .correctOption("B").build(),
                    QCMQuestion.builder().section(reactFinalQcm)
                            .questionText("What is JSX?")
                            .optionA("A database query language").optionB("A CSS preprocessor")
                            .optionC("A syntax extension for JavaScript").optionD("A testing framework")
                            .correctOption("C").build()
            ));

            // HR Compliance questions
            questionRepository.saveAll(List.of(
                    QCMQuestion.builder().section(hrFinalQcm)
                            .questionText("What should you do if you witness harassment?")
                            .optionA("Ignore it").optionB("Report to HR")
                            .optionC("Confront the harasser alone").optionD("Post about it online")
                            .correctOption("B").build(),
                    QCMQuestion.builder().section(hrFinalQcm)
                            .questionText("How often must compliance training be completed?")
                            .optionA("Once in your career").optionB("Every 5 years")
                            .optionC("Annually").optionD("Monthly")
                            .correctOption("C").build()
            ));
            log.info("Created 13 QCM questions");

            // ── Course-Department Assignments ────────────────────────
            assignmentRepository.save(CourseDepartmentAssignment.builder()
                    .course(javaBasics).department(backend)
                    .deadlineDate(LocalDate.now().plusMonths(2)).build());
            assignmentRepository.save(CourseDepartmentAssignment.builder()
                    .course(javaBasics).department(frontend)
                    .deadlineDate(LocalDate.now().plusMonths(3)).build());
            assignmentRepository.save(CourseDepartmentAssignment.builder()
                    .course(springBoot).department(backend)
                    .deadlineDate(LocalDate.now().plusMonths(4)).build());
            assignmentRepository.save(CourseDepartmentAssignment.builder()
                    .course(reactCourse).department(frontend)
                    .deadlineDate(LocalDate.now().plusMonths(3)).build());
            assignmentRepository.save(CourseDepartmentAssignment.builder()
                    .course(hrCompliance).department(hr)
                    .deadlineDate(LocalDate.now().minusDays(10)).build()); // overdue!
            assignmentRepository.save(CourseDepartmentAssignment.builder()
                    .course(hrCompliance).department(marketing)
                    .deadlineDate(LocalDate.now().plusMonths(1)).build());
            log.info("Created 6 course-department assignments");

            // ── Employee Progress ────────────────────────────────────
            // John completed all Java sections
            progressRepository.saveAll(List.of(
                    EmployeeCourseProgress.builder()
                            .employee(john).course(javaBasics).section(javaIntro)
                            .status(ProgressStatus.COMPLETED).completedAt(LocalDateTime.now().minusDays(20)).build(),
                    EmployeeCourseProgress.builder()
                            .employee(john).course(javaBasics).section(javaOop)
                            .status(ProgressStatus.COMPLETED).completedAt(LocalDateTime.now().minusDays(15)).build(),
                    EmployeeCourseProgress.builder()
                            .employee(john).course(javaBasics).section(javaCollections)
                            .status(ProgressStatus.COMPLETED).completedAt(LocalDateTime.now().minusDays(10)).build(),
                    EmployeeCourseProgress.builder()
                            .employee(john).course(javaBasics).section(javaPracticeQcm)
                            .status(ProgressStatus.COMPLETED).completedAt(LocalDateTime.now().minusDays(7)).build(),
                    EmployeeCourseProgress.builder()
                            .employee(john).course(javaBasics).section(javaFinalQcm)
                            .status(ProgressStatus.COMPLETED).completedAt(LocalDateTime.now().minusDays(5)).build()
            ));

            // Jane started Java (partial progress)
            progressRepository.saveAll(List.of(
                    EmployeeCourseProgress.builder()
                            .employee(jane).course(javaBasics).section(javaIntro)
                            .status(ProgressStatus.COMPLETED).completedAt(LocalDateTime.now().minusDays(3)).build(),
                    EmployeeCourseProgress.builder()
                            .employee(jane).course(javaBasics).section(javaOop)
                            .status(ProgressStatus.IN_PROGRESS).build()
            ));

            // Charlie completed Java intro only
            progressRepository.save(EmployeeCourseProgress.builder()
                    .employee(charlie).course(javaBasics).section(javaIntro)
                    .status(ProgressStatus.COMPLETED).completedAt(LocalDateTime.now().minusDays(1)).build());
            log.info("Created 8 progress records");

            // ── QCM Attempts ─────────────────────────────────────────
            // John's attempts on Java Final
            attemptRepository.saveAll(List.of(
                    EmployeeQCMAttempt.builder()
                            .employee(john).section(javaFinalQcm)
                            .attemptNumber(1).score(50).takenAt(LocalDateTime.now().minusDays(6)).build(),
                    EmployeeQCMAttempt.builder()
                            .employee(john).section(javaFinalQcm)
                            .attemptNumber(2).score(85).takenAt(LocalDateTime.now().minusDays(5)).build()
            ));

            // John's attempt on Java Practice
            attemptRepository.save(EmployeeQCMAttempt.builder()
                    .employee(john).section(javaPracticeQcm)
                    .attemptNumber(1).score(67).takenAt(LocalDateTime.now().minusDays(8)).build());
            log.info("Created 3 QCM attempts");

            // ── Grades ───────────────────────────────────────────────
            gradeRepository.save(EmployeeCourseGrade.builder()
                    .employee(john).course(javaBasics)
                    .finalScore(85).passed(true)
                    .updatedAt(LocalDateTime.now().minusDays(5)).build());
            log.info("Created 1 course grade");

            // ── Certificates ─────────────────────────────────────────
            Certificate cert = certificateRepository.save(Certificate.builder()
                    .employee(john).course(javaBasics)
                    .certificateCode(UUID.randomUUID().toString())
                    .qrCodeUrl("/api/certificates/verify/" + UUID.randomUUID().toString())
                    .issuedAt(LocalDateTime.now().minusDays(5))
                    .build());
            log.info("Created 1 certificate: {}", cert.getCertificateCode());

            // ── Audit Logs ───────────────────────────────────────────
            auditLogRepository.saveAll(List.of(
                    AuditLog.builder()
                            .userId(admin.getId()).actionType(ActionType.CREATE)
                            .entityType("Course").entityId(javaBasics.getId())
                            .newValue("{\"title\":\"Java Fundamentals\",\"status\":\"PUBLISHED\"}").build(),
                    AuditLog.builder()
                            .userId(admin.getId()).actionType(ActionType.CREATE)
                            .entityType("Course").entityId(springBoot.getId())
                            .newValue("{\"title\":\"Spring Boot Mastery\",\"status\":\"PUBLISHED\"}").build(),
                    AuditLog.builder()
                            .userId(admin.getId()).actionType(ActionType.CREATE)
                            .entityType("CourseAssignment").entityId(1L)
                            .newValue("{\"courseId\":" + javaBasics.getId() + ",\"departmentId\":" + backend.getId() + "}").build()
            ));
            log.info("Created 3 audit log entries");

            log.info("=== Dev seed data initialization complete ===");
            log.info("Admin login: admin@poodle.com / Admin@123");
            log.info("Employee login: john.doe@poodle.com / Employee@123");
        };
    }
}
