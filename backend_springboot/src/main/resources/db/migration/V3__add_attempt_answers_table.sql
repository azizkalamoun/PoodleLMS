-- =============================================
-- V3__add_attempt_answers_table.sql
-- Poodle LMS - Add employee_qcm_attempt_answers table
-- =============================================

CREATE TABLE IF NOT EXISTS employee_qcm_attempt_answers (
    id              BIGSERIAL       PRIMARY KEY,
    attempt_id      BIGINT          NOT NULL REFERENCES employee_qcm_attempts(id) ON DELETE CASCADE,
    question_id     BIGINT          NOT NULL REFERENCES qcm_questions(id) ON DELETE CASCADE,
    selected_option VARCHAR(1),
    correct         BOOLEAN         NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_attempt_answer_attempt ON employee_qcm_attempt_answers(attempt_id);
CREATE INDEX IF NOT EXISTS idx_attempt_answer_question ON employee_qcm_attempt_answers(question_id);
