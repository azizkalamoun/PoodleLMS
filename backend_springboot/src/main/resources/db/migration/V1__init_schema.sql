-- =============================================
-- V1__init_schema.sql
-- Poodle LMS - Initial Database Schema
-- =============================================

-- Departments (hierarchical)
CREATE TABLE departments (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(200)    NOT NULL,
    parent_department_id BIGINT     REFERENCES departments(id),
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_department_parent ON departments(parent_department_id);
CREATE INDEX idx_department_name ON departments(name);

-- Employees
CREATE TABLE employees (
    id              BIGSERIAL       PRIMARY KEY,
    first_name      VARCHAR(100)    NOT NULL,
    last_name       VARCHAR(100)    NOT NULL,
    email           VARCHAR(255)    NOT NULL,
    password        VARCHAR(255)    NOT NULL,
    role            VARCHAR(20)     NOT NULL CHECK (role IN ('ROLE_ADMIN', 'ROLE_EMPLOYEE')),
    department_id   BIGINT          REFERENCES departments(id),
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    UNIQUE (email)
);

-- Index for department lookups
CREATE INDEX idx_employee_department ON employees(department_id);

-- Courses
CREATE TABLE courses (
    id              BIGSERIAL       PRIMARY KEY,
    title           VARCHAR(255)    NOT NULL,
    description     TEXT,
    status          VARCHAR(20)     NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    passing_score   INTEGER         NOT NULL DEFAULT 70,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_course_status ON courses(status);
CREATE INDEX idx_course_title ON courses(title);

-- Course Prerequisites
CREATE TABLE course_prerequisites (
    id                      BIGSERIAL   PRIMARY KEY,
    course_id               BIGINT      NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    prerequisite_course_id  BIGINT      NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT uk_course_prerequisite UNIQUE (course_id, prerequisite_course_id),
    CONSTRAINT chk_no_self_prerequisite CHECK (course_id <> prerequisite_course_id)
);

CREATE INDEX idx_prereq_course ON course_prerequisites(course_id);
CREATE INDEX idx_prereq_prerequisite ON course_prerequisites(prerequisite_course_id);

-- Course Sections
CREATE TABLE course_sections (
    id              BIGSERIAL       PRIMARY KEY,
    course_id       BIGINT          NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    title           VARCHAR(255)    NOT NULL,
    content_type    VARCHAR(20)     NOT NULL CHECK (content_type IN ('VIDEO', 'AUDIO', 'PDF', 'TEXT', 'QCM')),
    content_url     TEXT,
    file_description TEXT,
    order_index     INTEGER         NOT NULL DEFAULT 0,
    qcm_type        VARCHAR(20)    CHECK (qcm_type IN ('PRACTICE', 'FINAL')),
    max_attempts    INTEGER,
    llm_draft_enabled BOOLEAN       NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_section_course ON course_sections(course_id);
CREATE INDEX idx_section_order ON course_sections(course_id, order_index);

-- Course Department Assignments
CREATE TABLE course_department_assignments (
    id              BIGSERIAL       PRIMARY KEY,
    course_id       BIGINT          NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    department_id   BIGINT          NOT NULL REFERENCES departments(id) ON DELETE CASCADE,
    deadline_date   DATE,
    CONSTRAINT uk_course_department UNIQUE (course_id, department_id)
);

CREATE INDEX idx_cda_course ON course_department_assignments(course_id);
CREATE INDEX idx_cda_department ON course_department_assignments(department_id);
CREATE INDEX idx_cda_deadline ON course_department_assignments(deadline_date);

-- QCM Questions
CREATE TABLE qcm_questions (
    id              BIGSERIAL       PRIMARY KEY,
    section_id      BIGINT          NOT NULL REFERENCES course_sections(id) ON DELETE CASCADE,
    question_text   TEXT            NOT NULL,
    option_a        VARCHAR(500)    NOT NULL,
    option_b        VARCHAR(500)    NOT NULL,
    option_c        VARCHAR(500)    NOT NULL,
    option_d        VARCHAR(500)    NOT NULL,
    correct_option  VARCHAR(1)      NOT NULL CHECK (correct_option IN ('A', 'B', 'C', 'D')),
    llm_generated   BOOLEAN         NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_qcm_section ON qcm_questions(section_id);

-- Employee Course Progress
CREATE TABLE employee_course_progress (
    id              BIGSERIAL       PRIMARY KEY,
    employee_id     BIGINT          NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    course_id       BIGINT          NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    section_id      BIGINT          NOT NULL REFERENCES course_sections(id) ON DELETE CASCADE,
    status          VARCHAR(20)     NOT NULL DEFAULT 'NOT_STARTED' CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED')),
    completed_at    TIMESTAMP,
    CONSTRAINT uk_employee_section_progress UNIQUE (employee_id, section_id)
);

CREATE INDEX idx_progress_employee ON employee_course_progress(employee_id);
CREATE INDEX idx_progress_course ON employee_course_progress(course_id);
CREATE INDEX idx_progress_section ON employee_course_progress(section_id);

-- Employee QCM Attempts
CREATE TABLE employee_qcm_attempts (
    id              BIGSERIAL       PRIMARY KEY,
    employee_id     BIGINT          NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    section_id      BIGINT          NOT NULL REFERENCES course_sections(id) ON DELETE CASCADE,
    attempt_number  INTEGER         NOT NULL,
    score           INTEGER         NOT NULL,
    taken_at        TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_attempt_employee ON employee_qcm_attempts(employee_id);
CREATE INDEX idx_attempt_section ON employee_qcm_attempts(section_id);

-- Employee Course Grades
CREATE TABLE employee_course_grades (
    id              BIGSERIAL       PRIMARY KEY,
    employee_id     BIGINT          NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    course_id       BIGINT          NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    final_score     INTEGER         NOT NULL,
    passed          BOOLEAN         NOT NULL DEFAULT FALSE,
    updated_at      TIMESTAMP,
    CONSTRAINT uk_employee_course_grade UNIQUE (employee_id, course_id)
);

CREATE INDEX idx_grade_employee ON employee_course_grades(employee_id);
CREATE INDEX idx_grade_course ON employee_course_grades(course_id);

-- Certificates
CREATE TABLE certificates (
    id                  BIGSERIAL       PRIMARY KEY,
    employee_id         BIGINT          NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    course_id           BIGINT          NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    certificate_code    VARCHAR(36)     NOT NULL UNIQUE,
    qr_code_url         VARCHAR(500),
    issued_at           TIMESTAMP       NOT NULL DEFAULT NOW(),
    revoked             BOOLEAN         NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_cert_employee ON certificates(employee_id);
CREATE INDEX idx_cert_course ON certificates(course_id);
CREATE UNIQUE INDEX idx_cert_code ON certificates(certificate_code);

-- Notifications
CREATE TABLE notifications (
    id              BIGSERIAL       PRIMARY KEY,
    type            VARCHAR(50)     NOT NULL,
    title           VARCHAR(255)    NOT NULL,
    message         TEXT            NOT NULL,
    related_entity_id   BIGINT,
    related_entity_type VARCHAR(50),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notif_type ON notifications(type);
CREATE INDEX idx_notif_created ON notifications(created_at);

-- Employee Notifications (join table for many-to-many relationship)
CREATE TABLE employee_notifications (
    id              BIGSERIAL       PRIMARY KEY,
    employee_id     BIGINT          NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    notification_id BIGINT          NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
    is_read         BOOLEAN         NOT NULL DEFAULT FALSE,
    read_at         TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_emp_notif_employee ON employee_notifications(employee_id);
CREATE INDEX idx_emp_notif_read ON employee_notifications(is_read);
CREATE INDEX idx_emp_notif_created ON employee_notifications(created_at);

-- Audit Logs
CREATE TABLE audit_logs (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    action_type     VARCHAR(20)     NOT NULL CHECK (action_type IN ('CREATE', 'UPDATE', 'DELETE', 'OVERRIDE', 'REVOKE')),
    entity_type     VARCHAR(100)    NOT NULL,
    entity_id       BIGINT          NOT NULL,
    timestamp       TIMESTAMP       NOT NULL DEFAULT NOW(),
    old_value       TEXT,
    new_value       TEXT
);

CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
