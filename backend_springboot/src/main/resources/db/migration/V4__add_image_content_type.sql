-- =============================================
-- V4__add_image_content_type.sql
-- Poodle LMS - Add IMAGE to course_sections content_type constraint
-- =============================================

-- Drop the existing constraint and recreate it with IMAGE support
ALTER TABLE course_sections 
DROP CONSTRAINT IF EXISTS course_sections_content_type_check;

ALTER TABLE course_sections 
ADD CONSTRAINT course_sections_content_type_check 
CHECK (content_type IN ('VIDEO', 'AUDIO', 'PDF', 'TEXT', 'QCM', 'IMAGE'));
