-- Seed default admin user for dev profile
-- Password: Admin@123
INSERT INTO employees (first_name, last_name, email, password, role, deleted)
VALUES (
    'System',
    'Admin',
    'admin@poodle.com',
    '$2a$12$LJ3gQPj5cPMaRjDTyuqYbeGi3ISoT.vBdKHZjm6RTqvATwxwTCnHy',
    'ROLE_ADMIN',
    FALSE
);
