-- =============================================
-- V5__reset_sequences_h2.sql
-- Reset H2 auto-increment sequences after seed data
-- This is H2-specific and should be ignored by PostgreSQL
-- =============================================

-- H2 uses ALTER TABLE...ALTER COLUMN...RESTART WITH syntax for identity resets
-- Set all sequences to start after the last inserted ID to prevent duplicates

ALTER TABLE departments ALTER COLUMN id RESTART WITH 19;
ALTER TABLE employees ALTER COLUMN id RESTART WITH 46;
ALTER TABLE courses ALTER COLUMN id RESTART WITH 14;
ALTER TABLE course_sections ALTER COLUMN id RESTART WITH 64;
ALTER TABLE qcm_questions ALTER COLUMN id RESTART WITH 68;
ALTER TABLE course_department_assignments ALTER COLUMN id RESTART WITH 32;
ALTER TABLE employee_course_progress ALTER COLUMN id RESTART WITH 60;
ALTER TABLE employee_qcm_attempts ALTER COLUMN id RESTART WITH 16;
ALTER TABLE employee_course_grades ALTER COLUMN id RESTART WITH 11;
ALTER TABLE certificates ALTER COLUMN id RESTART WITH 10;
ALTER TABLE notifications ALTER COLUMN id RESTART WITH 54;
ALTER TABLE employee_notifications ALTER COLUMN id RESTART WITH 54;
ALTER TABLE audit_logs ALTER COLUMN id RESTART WITH 24;
