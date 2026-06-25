-- =============================================
-- V2__seed_test_data.sql
-- Poodle LMS - Comprehensive Test Data
-- =============================================

-- =========== DEPARTMENTS ===========
-- 6 top-level + 12 sub-departments = 18 total
INSERT INTO departments (id, name, parent_department_id, deleted) VALUES
(1,  'Engineering',              NULL, FALSE),
(2,  'Software Development',     1,    FALSE),
(3,  'Quality Assurance',        1,    FALSE),
(4,  'DevOps & Infrastructure',  1,    FALSE),
(5,  'Business',                 NULL, FALSE),
(6,  'Sales',                    5,    FALSE),
(7,  'Marketing',                5,    FALSE),
(8,  'Finance',                  NULL, FALSE),
(9,  'Accounting',               8,    FALSE),
(10, 'Financial Analysis',       8,    FALSE),
(11, 'Human Resources',          NULL, FALSE),
(12, 'Talent Acquisition',       11,   FALSE),
(13, 'Employee Relations',       11,   FALSE),
(14, 'Legal & Compliance',       NULL, FALSE),
(15, 'Data & Analytics',         1,    FALSE),
(16, 'Product Management',       NULL, FALSE),
(17, 'Customer Success',         5,    FALSE),
(18, 'Security',                 1,    FALSE);

-- =========== EMPLOYEES ===========
-- 5 admins + 40 employees = 45 total
INSERT INTO employees (id, first_name, last_name, email, password, role, department_id, deleted) VALUES
-- Admins
(1,  'Admin',    'User',       'admin@example.com',              '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_ADMIN',    1,  FALSE),
(2,  'John',     'Instructor', 'john.instructor@example.com',    '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_ADMIN',    2,  FALSE),
(3,  'Jane',     'Trainer',    'jane.trainer@example.com',       '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_ADMIN',    3,  FALSE),
(4,  'Marcus',   'Reid',       'marcus.reid@example.com',        '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_ADMIN',    11, FALSE),
(5,  'Sandra',   'Okafor',     'sandra.okafor@example.com',      '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_ADMIN',    16, FALSE),
-- Software Development
(6,  'Alice',    'Developer',  'alice@example.com',              '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 2,  FALSE),
(7,  'Bob',      'Developer',  'bob@example.com',                '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 2,  FALSE),
(8,  'Carlos',   'Mendes',     'carlos.mendes@example.com',      '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 2,  FALSE),
(9,  'Diana',    'Wu',         'diana.wu@example.com',           '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 2,  FALSE),
(10, 'Ethan',    'Brooks',     'ethan.brooks@example.com',       '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 2,  FALSE),
(11, 'Fatima',   'Zahra',      'fatima.zahra@example.com',       '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 2,  FALSE),
-- QA
(12, 'Charlie',  'QA',         'charlie@example.com',            '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 3,  FALSE),
(13, 'Grace',    'Kim',        'grace.kim@example.com',          '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 3,  FALSE),
(14, 'Henry',    'Patel',      'henry.patel@example.com',        '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 3,  FALSE),
-- DevOps
(15, 'Ivan',     'Petrov',     'ivan.petrov@example.com',        '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 4,  FALSE),
(16, 'Julia',    'Nguyen',     'julia.nguyen@example.com',       '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 4,  FALSE),
(17, 'Kevin',    'Osei',       'kevin.osei@example.com',         '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 4,  FALSE),
-- Sales
(18, 'Laura',    'Sales',      'laura@example.com',              '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 6,  FALSE),
(19, 'Mike',     'Torres',     'mike.torres@example.com',        '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 6,  FALSE),
(20, 'Nina',     'Bakker',     'nina.bakker@example.com',        '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 6,  FALSE),
(21, 'Omar',     'Hassan',     'omar.hassan@example.com',        '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 6,  FALSE),
-- Marketing
(22, 'Paula',    'Schmidt',    'paula.schmidt@example.com',      '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 7,  FALSE),
(23, 'Quinn',    'Adeyemi',    'quinn.adeyemi@example.com',      '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 7,  FALSE),
(24, 'Rachel',   'Johansson',  'rachel.johansson@example.com',   '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 7,  FALSE),
-- Finance / Accounting
(25, 'Samuel',   'Levy',       'samuel.levy@example.com',        '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 9,  FALSE),
(26, 'Tina',     'Rossi',      'tina.rossi@example.com',         '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 9,  FALSE),
(27, 'Umar',     'Diallo',     'umar.diallo@example.com',        '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 10, FALSE),
-- HR
(28, 'Vera',     'Lindqvist',  'vera.lindqvist@example.com',     '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 12, FALSE),
(29, 'William',  'Tan',        'william.tan@example.com',        '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 13, FALSE),
(30, 'Xena',     'Moreau',     'xena.moreau@example.com',        '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 12, FALSE),
-- Legal
(31, 'Yusuf',    'Al-Amin',    'yusuf.alamin@example.com',       '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 14, FALSE),
(32, 'Zara',     'Novak',      'zara.novak@example.com',         '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 14, FALSE),
-- Data & Analytics
(33, 'Aaron',    'Park',       'aaron.park@example.com',         '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 15, FALSE),
(34, 'Beatrice', 'Fontaine',   'beatrice.fontaine@example.com',  '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 15, FALSE),
(35, 'Cyrus',    'Amiri',      'cyrus.amiri@example.com',        '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 15, FALSE),
-- Product Management
(36, 'Demi',     'Vasquez',    'demi.vasquez@example.com',       '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 16, FALSE),
(37, 'Elias',    'Bergmann',   'elias.bergmann@example.com',     '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 16, FALSE),
-- Customer Success
(38, 'Fiona',    'Castillo',   'fiona.castillo@example.com',     '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 17, FALSE),
(39, 'George',   'Nkosi',      'george.nkosi@example.com',       '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 17, FALSE),
(40, 'Hana',     'Suzuki',     'hana.suzuki@example.com',        '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 17, FALSE),
-- Security
(41, 'Igor',     'Volkov',     'igor.volkov@example.com',        '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 18, FALSE),
(42, 'Jade',     'Owens',      'jade.owens@example.com',         '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 18, FALSE),
-- Soft-deleted (to test filter logic)
(43, 'Karl',     'Deleted',    'karl.deleted@example.com',       '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 2,  TRUE),
(44, 'Lena',     'Deleted',    'lena.deleted@example.com',       '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 6,  TRUE),
(45, 'Mona',     'Legacy',     'mona.legacy@example.com',        '$2a$12$C6RfPynts2CkCbwFg03nsuDcOTt.mG.tuIOxsdqedGdxJ5UFVi.fW', 'ROLE_EMPLOYEE', 11, TRUE);

-- =========== COURSES ===========
-- 12 active + 1 soft-deleted = 13 total
INSERT INTO courses (id, title, description, status, passing_score, deleted) VALUES
(1,  'Java Programming Fundamentals',     'Learn the basics of Java programming including OOP concepts, data structures, and best practices.',               'PUBLISHED', 70, FALSE),
(2,  'Spring Boot Masterclass',           'Advanced Spring Boot course covering REST APIs, microservices, security, and deployment strategies.',              'PUBLISHED', 70, FALSE),
(3,  'Database Design with PostgreSQL',   'Comprehensive guide to designing and optimizing relational databases using PostgreSQL.',                            'PUBLISHED', 70, FALSE),
(4,  'Quality Assurance & Testing',       'Learn manual and automated testing techniques including JUnit, Selenium, and test-driven development.',             'PUBLISHED', 70, FALSE),
(5,  'Angular Web Development',           'Complete Angular course covering components, services, routing, and state management.',                             'PUBLISHED', 70, FALSE),
(6,  'Docker & Kubernetes Essentials',    'Containerization and orchestration fundamentals: build, ship, and run applications at scale using Docker and K8s.','PUBLISHED', 75, FALSE),
(7,  'Cybersecurity Fundamentals',        'Introduction to information security, threat modeling, OWASP Top 10, and secure coding practices.',                 'PUBLISHED', 80, FALSE),
(8,  'Data Analysis with Python',         'Use Python, Pandas, and Matplotlib to analyze datasets, visualize trends, and generate actionable insights.',       'PUBLISHED', 70, FALSE),
(9,  'Agile & Scrum Methodology',         'Master agile principles, Scrum ceremonies, backlog management, and cross-functional team collaboration.',           'PUBLISHED', 70, FALSE),
(10, 'Financial Literacy for Employees',  'Understand company financial statements, budgeting cycles, expense management, and basic accounting concepts.',     'PUBLISHED', 65, FALSE),
(11, 'Leadership & Communication Skills', 'Develop leadership presence, active listening, conflict resolution, and effective written and verbal communication.','DRAFT',     70, FALSE),
(12, 'GDPR & Data Privacy Compliance',    'Comprehensive overview of GDPR obligations, data subject rights, breach reporting, and privacy-by-design.',         'DRAFT',     80, FALSE),
(13, 'Legacy Flash Course',               'Outdated course built on Flash — archived and no longer relevant.',                                                 'ARCHIVED',  60, TRUE);

-- =========== COURSE PREREQUISITES ===========
INSERT INTO course_prerequisites (course_id, prerequisite_course_id) VALUES
(2,  1),   -- Spring Boot requires Java Fundamentals
(5,  1),   -- Angular requires Java Fundamentals
(6,  2),   -- Docker/K8s requires Spring Boot
(7,  3),   -- Cybersecurity requires Database Design
(8,  3),   -- Data Analysis benefits from DB knowledge
(12, 10);  -- GDPR requires Financial Literacy

-- =========== COURSE SECTIONS ===========

-- Java Programming Fundamentals (Course 1) — 6 sections
INSERT INTO course_sections (id, course_id, title, content_type, content_url, file_description, order_index, qcm_type, max_attempts, llm_draft_enabled) VALUES
(1,  1, 'Introduction to Java',             'TEXT', 'Java is a high-level, class-based, object-oriented programming language designed with a "write once, run anywhere" philosophy.',                                              'Basic Java introduction and history',                             0, 'PRACTICE', 3, FALSE),
(2,  1, 'Variables and Data Types',          'TEXT', 'Java supports primitive types (int, double, boolean, char) and reference types (String, arrays, objects). Understanding type safety is fundamental to Java development.',   'Comprehensive guide to Java data types and variable declaration',  1, 'PRACTICE', 3, FALSE),
(3,  1, 'Control Flow Statements',           'TEXT', 'Master if-else, switch, for, while, and do-while loops for controlling program execution flow and handling conditional logic.',                                               'Control flow structures and loop examples',                       2, 'PRACTICE', 3, FALSE),
(4,  1, 'Object-Oriented Programming',       'TEXT', 'Learn about classes, objects, inheritance, polymorphism, encapsulation, and abstraction — the four pillars of OOP that Java is built on.',                                   'Core OOP concepts with real-world examples',                      3, 'PRACTICE', 3, FALSE),
(5,  1, 'Collections and Generics',          'TEXT', 'Explore the Java Collections Framework including List, Set, Map, Queue, and how generics provide type-safe containers for your data.',                                        'Java Collections Framework and Generics guide',                   4, 'PRACTICE', 3, FALSE),
(6,  1, 'Java Programming Final Exam',       'QCM',  NULL,                                                                                                                                                                        'Final assessment covering all Java fundamentals',                 5, 'FINAL',    2, TRUE);

-- Spring Boot Masterclass (Course 2) — 6 sections
INSERT INTO course_sections (id, course_id, title, content_type, content_url, file_description, order_index, qcm_type, max_attempts, llm_draft_enabled) VALUES
(7,  2, 'Spring Framework Overview',         'TEXT', 'Spring is a lightweight IoC container for enterprise Java. Learn dependency injection, the application context, and how Spring manages your beans.',                          'Spring framework fundamentals and architecture',                  0, 'PRACTICE', 3, FALSE),
(8,  2, 'Building REST APIs',                'TEXT', 'Create RESTful web services with Spring Boot. Learn about @RestController, @RequestMapping, @GetMapping/@PostMapping, request/response handling, and status codes.',         'REST API design patterns and implementation',                     1, 'PRACTICE', 3, FALSE),
(9,  2, 'Security with Spring Security',     'TEXT', 'Implement authentication and authorization using Spring Security. Covers JWT, OAuth2, role-based access control, and protecting endpoints.',                                  'Spring Security and JWT integration guide',                       2, 'PRACTICE', 3, FALSE),
(10, 2, 'Database Integration with JPA',     'TEXT', 'Use Spring Data JPA with Hibernate. Learn about entities, repositories, JPQL, query derivation, transactions, and database migrations with Flyway.',                        'JPA and Hibernate integration guide',                             3, 'PRACTICE', 3, FALSE),
(11, 2, 'Microservices Architecture',        'TEXT', 'Design and deploy microservices using Spring Boot. Cover service discovery with Eureka, load balancing, API gateways, and communication patterns via REST and Kafka.',       'Microservices patterns and best practices',                       4, 'PRACTICE', 3, FALSE),
(12, 2, 'Spring Boot Final Exam',            'QCM',  NULL,                                                                                                                                                                        'Comprehensive Spring Boot assessment',                            5, 'FINAL',    2, TRUE);

-- Database Design with PostgreSQL (Course 3) — 6 sections
INSERT INTO course_sections (id, course_id, title, content_type, content_url, file_description, order_index, qcm_type, max_attempts, llm_draft_enabled) VALUES
(13, 3, 'Relational Database Concepts',      'TEXT', 'Understand tables, rows, columns, primary keys, foreign keys, and relationships. Learn normalization forms (1NF, 2NF, 3NF) and when to denormalize.',                       'Database fundamentals and design theory',                         0, 'PRACTICE', 3, FALSE),
(14, 3, 'SQL Query Basics',                  'TEXT', 'Write SELECT, INSERT, UPDATE, and DELETE statements. Learn WHERE, ORDER BY, GROUP BY, HAVING, and joining multiple tables with INNER, LEFT, RIGHT, and FULL JOINs.',        'SQL fundamentals and query optimization',                         1, 'PRACTICE', 3, FALSE),
(15, 3, 'Advanced SQL Techniques',           'TEXT', 'Master subqueries, CTEs, window functions (ROW_NUMBER, RANK, LAG, LEAD), recursive queries, and complex join patterns for analytical workloads.',                            'Advanced SQL patterns and optimization techniques',               2, 'PRACTICE', 3, FALSE),
(16, 3, 'Indexes & Query Optimization',      'TEXT', 'Learn B-tree and hash indexes, EXPLAIN ANALYZE, query planner statistics, covering indexes, partial indexes, and strategies for optimizing slow queries.',                   'PostgreSQL performance tuning and indexing',                      3, 'PRACTICE', 3, FALSE),
(17, 3, 'Database Administration',           'TEXT', 'User management, VACUUM, ANALYZE, pg_stat_* views, logical replication, point-in-time recovery, and backup strategies with pg_dump.',                                       'PostgreSQL administration and maintenance',                       4, 'PRACTICE', 3, FALSE),
(18, 3, 'Database Design Final Exam',        'QCM',  NULL,                                                                                                                                                                        'Final assessment on database design and SQL',                    5, 'FINAL',    2, TRUE);

-- Quality Assurance & Testing (Course 4) — 6 sections
INSERT INTO course_sections (id, course_id, title, content_type, content_url, file_description, order_index, qcm_type, max_attempts, llm_draft_enabled) VALUES
(19, 4, 'Testing Fundamentals',              'TEXT', 'Types of testing: unit, integration, system, end-to-end, regression, smoke, and acceptance. Test plans, test cases, traceability matrices, and exit criteria.',              'QA fundamentals and testing types',                               0, 'PRACTICE', 3, FALSE),
(20, 4, 'Manual Testing Techniques',         'TEXT', 'Test case design: equivalence partitioning, boundary value analysis, decision tables, and state transition. Bug lifecycle and effective bug reporting in Jira.',              'Manual testing methodologies and best practices',                 1, 'PRACTICE', 3, FALSE),
(21, 4, 'Automated Testing with JUnit',      'TEXT', 'Write unit tests with JUnit 5, parameterized tests, Mockito mocking, AssertJ assertions, and test coverage measurement with JaCoCo.',                                        'Unit testing frameworks and practices',                           2, 'PRACTICE', 3, FALSE),
(22, 4, 'Selenium WebDriver for UI Testing', 'TEXT', 'Automate web application testing with Selenium 4. Page Object Model, explicit/implicit waits, cross-browser testing, and integration with TestNG and Maven.',               'Web automation testing with Selenium',                            3, 'PRACTICE', 3, FALSE),
(23, 4, 'API Testing with REST Assured',     'TEXT', 'Test REST APIs using REST Assured: request specification, response validation, authentication headers, JSON schema validation, and data-driven test design.',                 'REST API testing techniques and REST Assured guide',              4, 'PRACTICE', 3, FALSE),
(24, 4, 'QA Final Certification Exam',       'QCM',  NULL,                                                                                                                                                                        'Comprehensive QA and testing assessment',                         5, 'FINAL',    2, TRUE);

-- Angular Web Development (Course 5) — 5 sections
INSERT INTO course_sections (id, course_id, title, content_type, content_url, file_description, order_index, qcm_type, max_attempts, llm_draft_enabled) VALUES
(25, 5, 'TypeScript & Angular Basics',       'TEXT', 'TypeScript types, interfaces, decorators. Angular CLI, modules, components, templates, and the component lifecycle hooks (ngOnInit, ngOnDestroy, etc.).',                    'Angular fundamentals and TypeScript primer',                      0, 'PRACTICE', 3, FALSE),
(26, 5, 'Data Binding & Directives',         'TEXT', 'One-way, two-way, and event binding. Built-in directives: *ngIf, *ngFor, *ngSwitch, [ngClass], [ngStyle]. Create custom attribute and structural directives.',              'Angular binding and directive system',                            1, 'PRACTICE', 3, FALSE),
(27, 5, 'Services, DI & HTTP Client',        'TEXT', 'Angular dependency injection system, singleton services, HttpClientModule, RxJS Observables, operators (map, switchMap, catchError), and reactive data flows.',              'Angular services and reactive programming with RxJS',             2, 'PRACTICE', 3, FALSE),
(28, 5, 'Routing & Lazy Loading',            'TEXT', 'Angular Router: route definitions, route guards (CanActivate, CanDeactivate), child routes, query params, lazy-loaded feature modules, and preloading strategies.',          'Angular routing architecture and lazy loading',                   3, 'PRACTICE', 3, FALSE),
(29, 5, 'Angular Final Exam',                'QCM',  NULL,                                                                                                                                                                        'Comprehensive Angular assessment',                                4, 'FINAL',    2, TRUE);

-- Docker & Kubernetes Essentials (Course 6) — 5 sections
INSERT INTO course_sections (id, course_id, title, content_type, content_url, file_description, order_index, qcm_type, max_attempts, llm_draft_enabled) VALUES
(30, 6, 'Docker Fundamentals',               'TEXT', 'Containers vs VMs, Docker architecture, Dockerfile directives (FROM, RUN, COPY, EXPOSE, CMD, ENTRYPOINT), image layers, and the Docker Hub registry.',                      'Docker basics, images, and containers',                           0, 'PRACTICE', 3, FALSE),
(31, 6, 'Docker Compose & Networking',       'TEXT', 'Multi-container applications with Docker Compose: services, volumes, networks, environment variables, health checks, and depends_on ordering.',                              'Docker Compose and container networking',                         1, 'PRACTICE', 3, FALSE),
(32, 6, 'Kubernetes Architecture',           'TEXT', 'K8s components: API server, etcd, scheduler, controller manager, kubelet. Pod lifecycle, namespaces, labels, selectors, and the declarative configuration model.',          'Kubernetes core concepts and cluster architecture',               2, 'PRACTICE', 3, FALSE),
(33, 6, 'Deployments, Services & Ingress',   'TEXT', 'Kubernetes Deployments, ReplicaSets, rolling updates, rollback strategies. ClusterIP, NodePort, LoadBalancer Services. Ingress controllers and TLS termination.',           'Kubernetes workloads and traffic management',                     3, 'PRACTICE', 3, FALSE),
(34, 6, 'Docker & K8s Final Exam',           'QCM',  NULL,                                                                                                                                                                        'Final assessment on containerization and orchestration',          4, 'FINAL',    2, TRUE);

-- Cybersecurity Fundamentals (Course 7) — 5 sections
INSERT INTO course_sections (id, course_id, title, content_type, content_url, file_description, order_index, qcm_type, max_attempts, llm_draft_enabled) VALUES
(35, 7, 'Security Concepts & Threat Landscape','TEXT','CIA triad, threat actors, attack vectors, vulnerability vs exploit, risk management frameworks (NIST, ISO 27001), and the security mindset.',                               'Information security foundations',                                0, 'PRACTICE', 3, FALSE),
(36, 7, 'OWASP Top 10',                      'TEXT', 'In-depth review of the OWASP Top 10: injection, broken authentication, XSS, IDOR, security misconfigurations, vulnerable components — with code examples.',                 'OWASP Top 10 vulnerabilities and mitigations',                    1, 'PRACTICE', 3, FALSE),
(37, 7, 'Network Security Basics',           'TEXT', 'Firewalls, DMZs, VPNs, TLS/SSL internals, certificate chains, DNS security, and common network attack techniques like man-in-the-middle and ARP spoofing.',                 'Network security fundamentals',                                   2, 'PRACTICE', 3, FALSE),
(38, 7, 'Secure Coding Practices',           'TEXT', 'Input validation, parameterized queries, output encoding, secrets management (Vault, env vars), dependency scanning, SAST/DAST tools, and security code review.',           'Secure development lifecycle and coding standards',               3, 'PRACTICE', 3, FALSE),
(39, 7, 'Cybersecurity Final Exam',          'QCM',  NULL,                                                                                                                                                                        'Final assessment on cybersecurity fundamentals',                  4, 'FINAL',    2, TRUE);

-- Data Analysis with Python (Course 8) — 5 sections
INSERT INTO course_sections (id, course_id, title, content_type, content_url, file_description, order_index, qcm_type, max_attempts, llm_draft_enabled) VALUES
(40, 8, 'Python & Jupyter Quickstart',       'TEXT', 'Python syntax refresher, virtual environments, pip, Jupyter notebooks, and how to structure a data analysis project from raw CSV to insight.',                               'Python setup and data analysis workflow',                         0, 'PRACTICE', 3, FALSE),
(41, 8, 'Data Wrangling with Pandas',        'TEXT', 'DataFrames and Series, reading/writing CSV/Excel/JSON, handling missing values, merging, groupby aggregations, and reshaping data with pivot tables and melt.',             'Pandas data manipulation and cleaning techniques',                1, 'PRACTICE', 3, FALSE),
(42, 8, 'Data Visualization',               'TEXT', 'Create line charts, bar plots, scatter plots, heatmaps, and histograms with Matplotlib and Seaborn. Storytelling with data and choosing the right chart type.',              'Data visualization with Matplotlib and Seaborn',                  2, 'PRACTICE', 3, FALSE),
(43, 8, 'Statistical Analysis Basics',       'TEXT', 'Descriptive statistics, distributions, correlation vs causation, hypothesis testing (t-test, chi-square), p-values, and confidence intervals with SciPy.',                   'Statistical foundations for data analysts',                       3, 'PRACTICE', 3, FALSE),
(44, 8, 'Data Analysis Final Exam',          'QCM',  NULL,                                                                                                                                                                        'Final assessment on Python data analysis',                        4, 'FINAL',    2, TRUE);

-- Agile & Scrum (Course 9) — 4 sections
INSERT INTO course_sections (id, course_id, title, content_type, content_url, file_description, order_index, qcm_type, max_attempts, llm_draft_enabled) VALUES
(45, 9, 'Agile Manifesto & Principles',      'TEXT', 'The 4 values and 12 principles of the Agile Manifesto. Lean thinking, iterative delivery, responding to change, and comparing Agile to Waterfall and hybrid approaches.',   'Agile philosophy and core principles',                            0, 'PRACTICE', 3, FALSE),
(46, 9, 'Scrum Framework Deep Dive',         'TEXT', 'Scrum roles (Product Owner, Scrum Master, Developers), events (Sprint, Planning, Daily, Review, Retrospective), and artifacts (Product Backlog, Sprint Backlog, Increment).','Scrum roles, events, and artifacts',                             1, 'PRACTICE', 3, FALSE),
(47, 9, 'Backlog Refinement & Estimation',   'TEXT', 'Writing effective user stories, acceptance criteria, story points, Planning Poker, T-shirt sizing, velocity tracking, and release planning.',                                 'User story writing and sprint estimation techniques',             2, 'PRACTICE', 3, FALSE),
(48, 9, 'Agile & Scrum Final Exam',          'QCM',  NULL,                                                                                                                                                                        'Comprehensive Agile and Scrum assessment',                        3, 'FINAL',    2, FALSE);

-- Financial Literacy (Course 10) — 4 sections
INSERT INTO course_sections (id, course_id, title, content_type, content_url, file_description, order_index, qcm_type, max_attempts, llm_draft_enabled) VALUES
(49, 10, 'Reading Financial Statements',     'TEXT', 'Understand the income statement, balance sheet, and cash flow statement. Learn how they interconnect and what key ratios (ROE, current ratio, EBITDA) tell you.',            'Corporate financial statements explained',                        0, 'PRACTICE', 3, FALSE),
(50, 10, 'Budgeting & Expense Management',   'TEXT', 'Departmental budgeting, cost center vs profit center, variance analysis, purchase order workflows, and responsible expense management for non-finance employees.',           'Budgeting fundamentals for team managers',                        1, 'PRACTICE', 3, FALSE),
(51, 10, 'Procurement & Vendor Management',  'TEXT', 'The procurement lifecycle, RFP/RFQ processes, vendor evaluation scorecards, contract types, SLAs, and avoiding conflicts of interest.',                                       'Procurement processes and vendor relations',                      2, 'PRACTICE', 3, FALSE),
(52, 10, 'Financial Literacy Final Exam',    'QCM',  NULL,                                                                                                                                                                        'Final assessment on financial literacy concepts',                 3, 'FINAL',    3, FALSE);

-- Leadership & Communication Skills (Course 11 — DRAFT) — 3 sections
INSERT INTO course_sections (id, course_id, title, content_type, content_url, file_description, order_index, qcm_type, max_attempts, llm_draft_enabled) VALUES
(53, 11, 'Leadership Styles & Self-Awareness','TEXT','Transformational, transactional, servant, and situational leadership models. Emotional intelligence (EQ) and self-assessment tools like DISC and StrengthsFinder.',          'Leadership models and emotional intelligence',                    0, 'PRACTICE', 3, TRUE),
(54, 11, 'Effective Communication',          'TEXT', 'Active listening, non-verbal cues, assertive vs aggressive vs passive communication, running effective meetings, and structuring written communication for clarity.',         'Communication skills for the workplace',                          1, 'PRACTICE', 3, TRUE),
(55, 11, 'Conflict Resolution & Feedback',   'TEXT', 'Interest-based negotiation, difficult conversations, the SBI feedback model (Situation-Behavior-Impact), peer reviews, and creating a culture of psychological safety.',    'Conflict management and feedback frameworks',                     2, 'PRACTICE', 3, TRUE);

-- GDPR & Data Privacy (Course 12 — DRAFT) — 3 sections
INSERT INTO course_sections (id, course_id, title, content_type, content_url, file_description, order_index, qcm_type, max_attempts, llm_draft_enabled) VALUES
(56, 12, 'GDPR Foundations',                 'TEXT', 'Scope of GDPR, key definitions (personal data, processing, controller, processor), lawful bases for processing, and how it applies to SaaS companies and employees.',       'GDPR legislation overview and applicability',                     0, 'PRACTICE', 3, FALSE),
(57, 12, 'Data Subject Rights & Obligations','TEXT', 'Right to access, rectification, erasure, portability, and objection. Responding to DSARs, record of processing activities (RoPA), and DPO responsibilities.',              'Data subject rights and controller obligations',                  1, 'PRACTICE', 3, FALSE),
(58, 12, 'Breach Notification & Privacy by Design','TEXT','72-hour breach notification rules, DPIA (Data Protection Impact Assessment), privacy-by-design principles, and building a data breach response playbook.',             'Data breach management and privacy engineering',                  2, 'PRACTICE', 3, FALSE);

-- New Sections (59-63) for Testing LLM Integration — 5 sections
INSERT INTO course_sections (id, course_id, title, content_type, content_url, file_description, order_index, qcm_type, max_attempts, llm_draft_enabled) VALUES
(59, 1, 'Java Advanced Patterns',           'TEXT', 'Explore design patterns, functional programming with streams, and advanced OOP concepts.',           'Advanced Java patterns and best practices',                       6, 'PRACTICE', 3, FALSE),
(60, 1, 'Java Testing & Debugging',         'TEXT', 'Learn debugging techniques, testing strategies, and profiling applications.',                    'Testing and debugging Java applications',                        7, 'PRACTICE', 3, FALSE),
(61, 2, 'Advanced Spring Concepts',         'TEXT', 'Explore advanced Spring features like AOP, annotations, and reactive programming.',               'Advanced Spring Boot concepts',                                   7, 'PRACTICE', 3, FALSE),
(62, 1, 'Java Performance Optimization',    'QCM',  NULL,                                                                                             'Performance tuning and optimization',                            8, 'PRACTICE', 3, TRUE),
(63, 1, 'Java Best Practices Review',       'QCM',  NULL,                                                                                             'Comprehensive review of Java best practices',                    9, 'PRACTICE', 3, TRUE);

-- =========== QCM QUESTIONS ===========

-- Java Final Exam (section 6) — 8 questions
INSERT INTO qcm_questions (id, section_id, question_text, option_a, option_b, option_c, option_d, correct_option, llm_generated) VALUES
(1,  6, 'What is the correct way to declare an integer variable in Java?',                 'var name;',                                              'int name;',                                                       'variable name;',                                   'declare int name;',                                          'B', FALSE),
(2,  6, 'Which of the following is NOT a primitive data type in Java?',                    'int',                                                    'double',                                                          'String',                                           'boolean',                                                    'C', FALSE),
(3,  6, 'What does the "this" keyword refer to in Java?',                                  'The parent class',                                       'The current object instance',                                     'The return value',                                 'The imported package',                                       'B', FALSE),
(4,  6, 'Which keyword is used to inherit from a class in Java?',                          'inherit',                                                'extends',                                                         'implements',                                       'super',                                                      'B', FALSE),
(5,  6, 'What is encapsulation?',                                                          'Hiding implementation details behind a public interface', 'Creating new classes from existing ones',                         'Copying objects by value',                         'Deleting unused variables',                                  'A', FALSE),
(6,  6, 'Which collection type guarantees element uniqueness?',                            'ArrayList',                                              'LinkedList',                                                      'HashSet',                                          'ArrayDeque',                                                 'C', FALSE),
(7,  6, 'What is the output of System.out.println(10 / 3) in Java?',                      '3.33',                                                   '3',                                                               '3.0',                                              'Compilation error',                                          'B', FALSE),
(8,  6, 'Which access modifier makes a member visible only within its own class?',         'public',                                                 'protected',                                                       'default',                                          'private',                                                    'D', FALSE);

-- Spring Boot Final Exam (section 12) — 8 questions
INSERT INTO qcm_questions (id, section_id, question_text, option_a, option_b, option_c, option_d, correct_option, llm_generated) VALUES
(9,  12, 'What is dependency injection in Spring?',                                        'Creating objects manually with new keyword',              'Spring container automatically wiring object dependencies',        'Deleting unused beans at runtime',                 'Importing third-party libraries',                            'B', FALSE),
(10, 12, 'Which annotation marks a class as a Spring-managed component?',                  '@Component',                                             '@Controller',                                                     '@Service',                                         'All of the above',                                           'D', FALSE),
(11, 12, 'What is the difference between @Service and @Repository?',                       'They are functionally identical',                        '@Service is for business logic, @Repository for data access',     '@Repository is deprecated in Spring 5',            '@Service cannot be autowired',                               'B', FALSE),
(12, 12, 'Which annotation maps HTTP GET requests to a controller method?',                '@PostMapping',                                           '@RequestParam',                                                   '@GetMapping',                                      '@ResponseBody',                                              'C', FALSE),
(13, 12, 'What does Spring Data JPA findAll() return?',                                    'A single Optional<T>',                                   'A List<T> of all entities',                                       'A Page<T> only when Pageable is provided',         'void',                                                       'B', FALSE),
(14, 12, 'Which file is the primary configuration source in a Spring Boot project?',       'pom.xml',                                                'web.xml',                                                         'application.properties or application.yml',        'beans.xml',                                                  'C', FALSE),
(15, 12, 'What is the purpose of @Transactional in Spring?',                               'To mark a class as a REST controller',                   'To ensure database operations are wrapped in a transaction',      'To enable CORS on an endpoint',                    'To schedule a method at a fixed rate',                       'B', FALSE),
(16, 12, 'Which mechanism is used to secure individual endpoints in Spring Security?',     '@EnableWebSecurity only',                                'HttpSecurity configuration with requestMatchers',                 '@Secured on the main class only',                  'application.properties security.endpoints property',         'B', FALSE);

-- Database Design Final Exam (section 18) — 8 questions
INSERT INTO qcm_questions (id, section_id, question_text, option_a, option_b, option_c, option_d, correct_option, llm_generated) VALUES
(17, 18, 'What does the ACID acronym stand for?',                                          'Atomicity, Consistency, Isolation, Durability',          'Accuracy, Consistency, Integrity, Dependability',                 'Atomicity, Concurrency, Isolation, Distribution',  'Availability, Consistency, Isolation, Durability',           'A', FALSE),
(18, 18, 'Which SQL clause filters results AFTER grouping?',                               'WHERE',                                                  'HAVING',                                                          'GROUP BY',                                         'FILTER',                                                     'B', FALSE),
(19, 18, 'What is a foreign key?',                                                         'The primary key of the current table',                   'A column referencing the primary key of another table',           'An index on a non-unique column',                  'A constraint that ensures values are unique',                 'B', FALSE),
(20, 18, 'Which normal form eliminates transitive dependencies?',                          'First Normal Form (1NF)',                                 'Second Normal Form (2NF)',                                        'Third Normal Form (3NF)',                           'Boyce-Codd Normal Form (BCNF)',                               'C', FALSE),
(21, 18, 'What does EXPLAIN ANALYZE do in PostgreSQL?',                                    'Drops all indexes to rebuild them',                      'Shows the execution plan with actual timing statistics',          'Compresses table storage',                         'Validates SQL syntax without executing it',                   'B', FALSE),
(22, 18, 'Which JOIN returns all rows from both tables with NULLs where there is no match?','INNER JOIN',                                            'LEFT JOIN',                                                       'RIGHT JOIN',                                       'FULL OUTER JOIN',                                            'D', FALSE),
(23, 18, 'What is a partial index in PostgreSQL?',                                         'An index covering only some columns',                    'An index built on a subset of rows matching a WHERE condition',   'An index that is not yet fully built',             'An index shared across multiple tables',                     'B', FALSE),
(24, 18, 'Which command permanently removes a table and all its data?',                    'DELETE FROM table_name',                                 'TRUNCATE table_name',                                             'DROP TABLE table_name',                            'REMOVE TABLE table_name',                                    'C', FALSE);

-- QA Final Exam (section 24) — 8 questions
INSERT INTO qcm_questions (id, section_id, question_text, option_a, option_b, option_c, option_d, correct_option, llm_generated) VALUES
(25, 24, 'What is the primary goal of regression testing?',                                'Testing new features only',                              'Ensuring previously working functionality has not broken',        'Measuring test coverage percentage',               'Checking non-functional requirements like performance',       'B', FALSE),
(26, 24, 'Which JUnit 5 annotation marks a test method?',                                  '@Test',                                                  '@TestMethod',                                                     '@RunWith',                                         '@Verify',                                                    'A', FALSE),
(27, 24, 'What does the Page Object Model pattern do in Selenium?',                        'Runs tests in parallel',                                 'Separates page UI interaction logic from test logic',             'Generates test reports automatically',             'Handles AJAX waiting automatically',                         'B', FALSE),
(28, 24, 'Which testing type validates the integration of two or more modules?',           'Unit testing',                                           'System testing',                                                  'Integration testing',                              'Acceptance testing',                                         'C', FALSE),
(29, 24, 'In REST Assured, which method sends a GET request?',                             '.post()',                                                 '.fetch()',                                                        '.get()',                                            '.retrieve()',                                                'C', FALSE),
(30, 24, 'What does a 422 HTTP status code indicate?',                                     'Not Found',                                              'Internal Server Error',                                           'Unprocessable Entity — validation failed',         'Service Unavailable',                                        'C', FALSE),
(31, 24, 'Which Mockito method stubs a return value for a method call?',                   'Mockito.spy()',                                           'Mockito.when().thenReturn()',                                     'Mockito.verify()',                                 'Mockito.mock() alone',                                       'B', FALSE),
(32, 24, 'What is boundary value analysis?',                                               'Testing every possible input value',                     'Testing only the middle value of an input range',                'Testing values at the edges of valid input ranges','Testing random values to find unexpected bugs',               'C', FALSE);

-- Angular Final Exam (section 29) — 6 questions
INSERT INTO qcm_questions (id, section_id, question_text, option_a, option_b, option_c, option_d, correct_option, llm_generated) VALUES
(33, 29, 'Which decorator defines an Angular component?',                                  '@NgModule',                                              '@Injectable',                                                     '@Component',                                       '@Directive',                                                 'C', FALSE),
(34, 29, 'What is the two-way data binding syntax in Angular?',                            '(click)',                                                 '[property]',                                                      '{{expression}}',                                   '[(ngModel)]',                                                'D', FALSE),
(35, 29, 'Which RxJS operator cancels the previous observable and subscribes to a new one?','mergeMap',                                              'concatMap',                                                       'switchMap',                                        'exhaustMap',                                                 'C', FALSE),
(36, 29, 'What is the purpose of Angular Route Guards?',                                   'Caching HTTP responses',                                 'Controlling navigation access to routes',                         'Lazy loading module styles',                       'Defining component templates',                               'B', FALSE),
(37, 29, 'Which lifecycle hook runs once after the component is initialized?',             'ngOnChanges',                                            'ngOnInit',                                                        'ngAfterViewInit',                                  'ngOnDestroy',                                                'B', FALSE),
(38, 29, 'What does the async pipe do in Angular templates?',                              'Creates a new Observable',                               'Auto-subscribes to an Observable/Promise and unwraps its value',  'Delays template rendering by one tick',            'Marks a method as asynchronous',                             'B', FALSE);

-- Docker & K8s Final Exam (section 34) — 6 questions
INSERT INTO qcm_questions (id, section_id, question_text, option_a, option_b, option_c, option_d, correct_option, llm_generated) VALUES
(39, 34, 'What is the difference between a Docker image and a container?',                 'They are the same thing',                                'An image is a read-only template; a container is a running instance','A container is stored in a registry',           'An image can only run on Linux',                             'B', FALSE),
(40, 34, 'Which Dockerfile instruction sets the base image?',                              'RUN',                                                    'BASE',                                                            'FROM',                                             'IMAGE',                                                      'C', FALSE),
(41, 34, 'What is a Kubernetes Pod?',                                                      'A physical server in the cluster',                       'The smallest deployable unit containing one or more containers',  'A load balancer configuration',                    'A namespace for isolating resources',                        'B', FALSE),
(42, 34, 'Which Kubernetes resource exposes a set of Pods to network traffic?',            'Deployment',                                             'ConfigMap',                                                       'Service',                                          'PersistentVolume',                                           'C', FALSE),
(43, 34, 'What does "kubectl rollout undo deployment/myapp" do?',                          'Deletes the deployment permanently',                     'Rolls back to the previous deployment revision',                 'Pauses the deployment rollout',                    'Scales the deployment to zero replicas',                     'B', FALSE),
(44, 34, 'What is the purpose of a Kubernetes Ingress?',                                   'Persistent storage for stateful apps',                   'Managing outbound cluster traffic',                               'Routing external HTTP/HTTPS traffic to Services',  'Running privileged containers',                              'C', FALSE);

-- Cybersecurity Final Exam (section 39) — 6 questions
INSERT INTO qcm_questions (id, section_id, question_text, option_a, option_b, option_c, option_d, correct_option, llm_generated) VALUES
(45, 39, 'What does the CIA triad stand for in information security?',                     'Control, Integrity, Authentication',                     'Confidentiality, Integrity, Availability',                        'Compliance, Isolation, Auditability',              'Cryptography, Identification, Authorization',                'B', FALSE),
(46, 39, 'Which OWASP Top 10 vulnerability occurs when user input is sent to an interpreter?','Broken Authentication',                               'Security Misconfiguration',                                       'Injection',                                        'Insecure Deserialization',                                   'C', FALSE),
(47, 39, 'What is a man-in-the-middle (MITM) attack?',                                    'Flooding a server with traffic',                         'An attacker intercepts communication between two parties',        'Gaining admin rights through privilege escalation','Injecting malicious scripts into a web page',                'B', FALSE),
(48, 39, 'Which practice best prevents SQL injection?',                                    'Input length validation only',                           'Encrypting the database at rest',                                 'Using parameterized queries / prepared statements','Disabling database logging',                                 'C', FALSE),
(49, 39, 'What is the purpose of a SAST tool?',                                            'Monitoring production traffic for anomalies',            'Scanning source code for vulnerabilities without executing it',    'Running penetration tests on live systems',        'Managing SSL certificates',                                  'B', FALSE),
(50, 39, 'What does TLS provide in a client-server connection?',                           'Data compression only',                                  'Encrypted and authenticated communication channel',               'Faster routing between servers',                   'User session management',                                    'B', FALSE);

-- Data Analysis Final Exam (section 44) — 6 questions
INSERT INTO qcm_questions (id, section_id, question_text, option_a, option_b, option_c, option_d, correct_option, llm_generated) VALUES
(51, 44, 'Which Pandas method removes rows with missing values?',                          'df.fill_na()',                                           'df.dropna()',                                                     'df.remove_null()',                                 'df.clean()',                                                 'B', FALSE),
(52, 44, 'What does df.groupby("col").mean() return?',                                     'The overall mean of the DataFrame',                      'A DataFrame aggregated by mean per group',                        'An error if col is non-numeric',                   'The median grouped by col',                                  'B', FALSE),
(53, 44, 'Which chart is best for showing correlation between two continuous variables?',  'Bar chart',                                              'Pie chart',                                                       'Scatter plot',                                     'Histogram',                                                  'C', FALSE),
(54, 44, 'What does a p-value less than 0.05 typically indicate?',                         'The result is practically significant',                  'The null hypothesis can be rejected at 5% significance',         'The sample size is too small',                     'The test failed',                                            'B', FALSE),
(55, 44, 'Which Pandas function reads a CSV file into a DataFrame?',                       'pd.load_csv()',                                          'pd.import_csv()',                                                 'pd.read_csv()',                                    'pd.open_csv()',                                              'C', FALSE),
(56, 44, 'What is the purpose of Seaborn heatmap?',                                        'Displaying geographic data on a map',                    'Visualizing a matrix of values with color encoding',              'Showing the distribution of a single variable',    'Plotting time series data',                                  'B', FALSE);

-- Agile Final Exam (section 48) — 6 questions
INSERT INTO qcm_questions (id, section_id, question_text, option_a, option_b, option_c, option_d, correct_option, llm_generated) VALUES
(57, 48, 'What is the primary role of the Product Owner in Scrum?',                        'Facilitating Scrum ceremonies',                          'Writing all the code for the sprint',                             'Maximizing product value by managing the Backlog', 'Reporting progress to stakeholders only',                    'C', FALSE),
(58, 48, 'How long is a typical Scrum Sprint?',                                            '1 week only',                                            '1-4 weeks',                                                       '3 months',                                         '6 months',                                                   'B', FALSE),
(59, 48, 'What is the Definition of Done (DoD)?',                                          'A list of features planned for the release',             'A shared checklist a backlog item must meet to be complete',      'The acceptance criteria for a single user story',  'The burndown target for the sprint',                         'B', FALSE),
(60, 48, 'Which Scrum event is the primary tool for team process improvement?',            'Sprint Review',                                          'Sprint Planning',                                                 'Daily Scrum',                                      'Sprint Retrospective',                                       'D', FALSE),
(61, 48, 'What are story points used for?',                                                'Tracking hours spent on a task',                         'Estimating relative effort and complexity of backlog items',      'Measuring velocity in lines of code',              'Assigning tasks to developers',                              'B', FALSE),
(62, 48, 'Which of the following is an Agile Manifesto value?',                            'Follow the plan above all else',                         'Comprehensive documentation over working software',               'Responding to change over following a plan',       'Contract negotiation over customer collaboration',           'C', FALSE);

-- Financial Literacy Final Exam (section 52) — 5 questions
INSERT INTO qcm_questions (id, section_id, question_text, option_a, option_b, option_c, option_d, correct_option, llm_generated) VALUES
(63, 52, 'Which financial statement shows revenues and expenses over a period?',           'Balance Sheet',                                          'Cash Flow Statement',                                             'Income Statement',                                 'Statement of Equity',                                        'C', FALSE),
(64, 52, 'What does EBITDA measure?',                                                      'Earnings after all deductions including tax and interest','Earnings before interest, taxes, depreciation, and amortization','Net cash from operations only',                   'Gross revenue before any deductions',                        'B', FALSE),
(65, 52, 'What is a cost center?',                                                         'A department that generates profit',                     'A department managing costs without generating direct revenue',   'An account for tracking vendor payments',          'A budget item for capital expenditures',                     'B', FALSE),
(66, 52, 'What is the purpose of a purchase order (PO)?',                                  'To record a payment already made',                       'To formally authorize a spend request to a vendor',               'To invoice a customer for services rendered',      'To track employee expense claims',                           'B', FALSE),
(67, 52, 'What is variance analysis?',                                                     'Comparing actual financial results to budgeted figures',  'Measuring the statistical spread of a data set',                 'Forecasting next quarter revenue',                 'Auditing vendor contracts for compliance',                   'A', FALSE);

-- =========== COURSE DEPARTMENT ASSIGNMENTS ===========
INSERT INTO course_department_assignments (id, course_id, department_id, deadline_date) VALUES
-- Java Fundamentals
(1,  1,  2,  '2026-04-30'),
(2,  1,  3,  '2026-04-30'),
(3,  1,  15, '2026-05-15'),
-- Spring Boot
(4,  2,  2,  '2026-05-15'),
(5,  2,  4,  '2026-05-31'),
-- Database
(6,  3,  2,  '2026-05-31'),
(7,  3,  15, '2026-05-31'),
(8,  3,  14, '2026-06-15'),
-- QA
(9,  4,  3,  '2026-04-15'),
-- Angular
(10, 5,  2,  '2026-06-30'),
-- Docker/K8s
(11, 6,  4,  '2026-05-01'),
(12, 6,  2,  '2026-05-31'),
-- Cybersecurity (mandatory for security + all Engineering + Legal)
(13, 7,  18, '2026-04-01'),
(14, 7,  2,  '2026-04-30'),
(15, 7,  3,  '2026-04-30'),
(16, 7,  4,  '2026-04-30'),
(17, 7,  14, '2026-04-30'),
-- Data Analysis
(18, 8,  15, '2026-05-15'),
(19, 8,  16, '2026-06-01'),
-- Agile/Scrum (company-wide)
(20, 9,  2,  '2026-06-30'),
(21, 9,  3,  '2026-06-30'),
(22, 9,  4,  '2026-06-30'),
(23, 9,  6,  '2026-06-30'),
(24, 9,  7,  '2026-06-30'),
(25, 9,  16, '2026-06-30'),
(26, 9,  17, '2026-06-30'),
-- Financial Literacy
(27, 10, 9,  '2026-07-31'),
(28, 10, 10, '2026-07-31'),
(29, 10, 11, '2026-07-31'),
(30, 10, 6,  '2026-07-31'),
(31, 10, 14, '2026-07-31');

-- =========== EMPLOYEE COURSE PROGRESS ===========
INSERT INTO employee_course_progress (id, employee_id, course_id, section_id, status, completed_at) VALUES
-- Alice (6) — Java: all 6 sections completed
(1,  6,  1, 1,  'COMPLETED',   DATEADD(day, -20, NOW())),
(2,  6,  1, 2,  'COMPLETED',   DATEADD(day, -18, NOW())),
(3,  6,  1, 3,  'COMPLETED',   DATEADD(day, -16, NOW())),
(4,  6,  1, 4,  'COMPLETED',   DATEADD(day, -14, NOW())),
(5,  6,  1, 5,  'COMPLETED',   DATEADD(day, -13, NOW())),
(6,  6,  1, 6,  'COMPLETED',   DATEADD(day, -12, NOW())),
-- Alice — Spring Boot: 3 of 6 done, 1 in progress
(7,  6,  2, 7,  'COMPLETED',   DATEADD(day, -10, NOW())),
(8,  6,  2, 8,  'COMPLETED',   DATEADD(day, -8, NOW())),
(9,  6,  2, 9,  'COMPLETED',   DATEADD(day, -6, NOW())),
(10, 6,  2, 10, 'IN_PROGRESS', NULL),
-- Bob (7) — Java: 2 done, 1 in progress
(11, 7,  1, 1,  'COMPLETED',   DATEADD(day, -15, NOW())),
(12, 7,  1, 2,  'COMPLETED',   DATEADD(day, -13, NOW())),
(13, 7,  1, 3,  'IN_PROGRESS', NULL),
-- Carlos (8) — Java: 4 done
(14, 8,  1, 1,  'COMPLETED',   DATEADD(day, -12, NOW())),
(15, 8,  1, 2,  'COMPLETED',   DATEADD(day, -11, NOW())),
(16, 8,  1, 3,  'COMPLETED',   DATEADD(day, -10, NOW())),
(17, 8,  1, 4,  'COMPLETED',   DATEADD(day, -9, NOW())),
-- Charlie (12) — QA: all 6 sections completed
(18, 12, 4, 19, 'COMPLETED',   DATEADD(day, -22, NOW())),
(19, 12, 4, 20, 'COMPLETED',   DATEADD(day, -20, NOW())),
(20, 12, 4, 21, 'COMPLETED',   DATEADD(day, -18, NOW())),
(21, 12, 4, 22, 'COMPLETED',   DATEADD(day, -16, NOW())),
(22, 12, 4, 23, 'COMPLETED',   DATEADD(day, -14, NOW())),
(23, 12, 4, 24, 'COMPLETED',   DATEADD(day, -12, NOW())),
-- Grace (13) — QA: all 6 completed
(24, 13, 4, 19, 'COMPLETED',   DATEADD(day, -15, NOW())),
(25, 13, 4, 20, 'COMPLETED',   DATEADD(day, -14, NOW())),
(26, 13, 4, 21, 'COMPLETED',   DATEADD(day, -13, NOW())),
(27, 13, 4, 22, 'COMPLETED',   DATEADD(day, -12, NOW())),
(28, 13, 4, 23, 'COMPLETED',   DATEADD(day, -11, NOW())),
(29, 13, 4, 24, 'COMPLETED',   DATEADD(day, -9, NOW())),
-- Diana Wu (9) — Database: all 6 completed
(30, 9,  3, 13, 'COMPLETED',   DATEADD(day, -25, NOW())),
(31, 9,  3, 14, 'COMPLETED',   DATEADD(day, -23, NOW())),
(32, 9,  3, 15, 'COMPLETED',   DATEADD(day, -21, NOW())),
(33, 9,  3, 16, 'COMPLETED',   DATEADD(day, -19, NOW())),
(34, 9,  3, 17, 'COMPLETED',   DATEADD(day, -17, NOW())),
(35, 9,  3, 18, 'COMPLETED',   DATEADD(day, -15, NOW())),
-- Fatima (11) — Database: 2 sections done
(36, 11, 3, 13, 'COMPLETED',   DATEADD(day, -8, NOW())),
(37, 11, 3, 14, 'IN_PROGRESS', NULL),
-- Ivan (15) — Docker: 2 done, 1 in progress
(38, 15, 6, 30, 'COMPLETED',   DATEADD(day, -5, NOW())),
(39, 15, 6, 31, 'COMPLETED',   DATEADD(day, -3, NOW())),
(40, 15, 6, 32, 'IN_PROGRESS', NULL),
-- Julia (16) — Docker: 1 done
(41, 16, 6, 30, 'COMPLETED',   DATEADD(day, -4, NOW())),
-- Aaron (33) — Data Analysis: all 5 done
(42, 33, 8, 40, 'COMPLETED',   DATEADD(day, -30, NOW())),
(43, 33, 8, 41, 'COMPLETED',   DATEADD(day, -28, NOW())),
(44, 33, 8, 42, 'COMPLETED',   DATEADD(day, -26, NOW())),
(45, 33, 8, 43, 'COMPLETED',   DATEADD(day, -24, NOW())),
(46, 33, 8, 44, 'COMPLETED',   DATEADD(day, -22, NOW())),
-- Beatrice (34) — Data Analysis: 3 done
(47, 34, 8, 40, 'COMPLETED',   DATEADD(day, -20, NOW())),
(48, 34, 8, 41, 'COMPLETED',   DATEADD(day, -18, NOW())),
(49, 34, 8, 42, 'COMPLETED',   DATEADD(day, -16, NOW())),
-- Laura (18) — Agile: 1 done, 1 in progress
(50, 18, 9, 45, 'COMPLETED',   DATEADD(day, -7, NOW())),
(51, 18, 9, 46, 'IN_PROGRESS', NULL),
-- Samuel (25) — Financial Literacy: 2 done
(52, 25, 10, 49, 'COMPLETED',  DATEADD(day, -14, NOW())),
(53, 25, 10, 50, 'COMPLETED',  DATEADD(day, -12, NOW())),
-- Yusuf (31) — Cybersecurity: all 5 done
(54, 31, 7, 35, 'COMPLETED',   DATEADD(day, -18, NOW())),
(55, 31, 7, 36, 'COMPLETED',   DATEADD(day, -16, NOW())),
(56, 31, 7, 37, 'COMPLETED',   DATEADD(day, -14, NOW())),
(57, 31, 7, 38, 'COMPLETED',   DATEADD(day, -12, NOW())),
(58, 31, 7, 39, 'COMPLETED',   DATEADD(day, -10, NOW())),
-- Igor (41) — Cybersecurity: not started yet (deadline approaching)
(59, 41, 7, 35, 'NOT_STARTED', NULL);

-- =========== EMPLOYEE QCM ATTEMPTS ===========
INSERT INTO employee_qcm_attempts (id, employee_id, section_id, attempt_number, score, taken_at) VALUES
-- Alice — Java final (section 6): failed first, passed second
(1,  6,  6,  1, 62, DATEADD(day, -13, NOW())),
(2,  6,  6,  2, 85, DATEADD(day, -12, NOW())),
-- Alice — Spring Boot practice (section 12)
(3,  6,  12, 1, 78, DATEADD(day, -6, NOW())),
-- Bob — Java final (section 6): failed, has 1 attempt left
(4,  7,  6,  1, 55, DATEADD(day, -10, NOW())),
-- Carlos — Java final: passed first try
(5,  8,  6,  1, 72, DATEADD(day, -8, NOW())),
-- Charlie — QA final (section 24): passed
(6,  12, 24, 1, 91, DATEADD(day, -12, NOW())),
-- Grace — QA final: passed
(7,  13, 24, 1, 80, DATEADD(day, -9, NOW())),
-- Diana Wu — Database final (section 18): passed
(8,  9,  18, 1, 88, DATEADD(day, -15, NOW())),
-- Aaron — Data Analysis final (section 44): passed
(9,  33, 44, 1, 74, DATEADD(day, -22, NOW())),
-- Ethan (10) — Java final: failed twice (max attempts reached)
(10, 10, 6,  1, 48, DATEADD(day, -20, NOW())),
(11, 10, 6,  2, 52, DATEADD(day, -18, NOW())),
-- Henry (14) — QA final: passed
(12, 14, 24, 1, 76, DATEADD(day, -5, NOW())),
-- Yusuf — Cybersecurity final (section 39): passed with high score
(13, 31, 39, 1, 92, DATEADD(day, -10, NOW())),
-- Beatrice — Data Analysis practice: in progress (no final yet)
(14, 34, 44, 1, 61, DATEADD(day, -3, NOW())),
-- Samuel — Financial Literacy practice (section 52)
(15, 25, 52, 1, 68, DATEADD(day, -5, NOW()));

-- =========== EMPLOYEE COURSE GRADES ===========
INSERT INTO employee_course_grades (id, employee_id, course_id, final_score, passed, updated_at) VALUES
(1,  6,  1, 85, TRUE,  DATEADD(day, -12, NOW())),   -- Alice passed Java
(2,  8,  1, 72, TRUE,  DATEADD(day, -8, NOW())),    -- Carlos passed Java
(3,  12, 4, 91, TRUE,  DATEADD(day, -12, NOW())),   -- Charlie passed QA
(4,  13, 4, 80, TRUE,  DATEADD(day, -9, NOW())),    -- Grace passed QA
(5,  14, 4, 76, TRUE,  DATEADD(day, -5, NOW())),    -- Henry passed QA
(6,  9,  3, 88, TRUE,  DATEADD(day, -15, NOW())),   -- Diana passed Database
(7,  33, 8, 74, TRUE,  DATEADD(day, -22, NOW())),   -- Aaron passed Data Analysis
(8,  31, 7, 92, TRUE,  DATEADD(day, -10, NOW())),   -- Yusuf passed Cybersecurity
(9,  7,  1, 55, FALSE, DATEADD(day, -10, NOW())),   -- Bob failed Java
(10, 10, 1, 52, FALSE, DATEADD(day, -18, NOW()));   -- Ethan failed Java (max attempts)

-- =========== CERTIFICATES ===========
INSERT INTO certificates (id, employee_id, course_id, certificate_code, qr_code_url, issued_at, revoked) VALUES
(1, 6,  1, 'CERT-JAVA-ALICE-2026-001',    'https://cdn.example.com/qr/CERT-JAVA-ALICE-2026-001.png',    DATEADD(day, -12, NOW()), FALSE),
(2, 8,  1, 'CERT-JAVA-CARLOS-2026-001',   'https://cdn.example.com/qr/CERT-JAVA-CARLOS-2026-001.png',   DATEADD(day, -8, NOW()),  FALSE),
(3, 12, 4, 'CERT-QA-CHARLIE-2026-001',    'https://cdn.example.com/qr/CERT-QA-CHARLIE-2026-001.png',    DATEADD(day, -12, NOW()), FALSE),
(4, 13, 4, 'CERT-QA-GRACE-2026-001',      'https://cdn.example.com/qr/CERT-QA-GRACE-2026-001.png',      DATEADD(day, -9, NOW()),  FALSE),
(5, 14, 4, 'CERT-QA-HENRY-2026-001',      'https://cdn.example.com/qr/CERT-QA-HENRY-2026-001.png',      DATEADD(day, -5, NOW()),  FALSE),
(6, 9,  3, 'CERT-DB-DIANA-2026-001',      'https://cdn.example.com/qr/CERT-DB-DIANA-2026-001.png',      DATEADD(day, -15, NOW()), FALSE),
(7, 33, 8, 'CERT-PY-AARON-2026-001',      'https://cdn.example.com/qr/CERT-PY-AARON-2026-001.png',      DATEADD(day, -22, NOW()), FALSE),
(8, 31, 7, 'CERT-SEC-YUSUF-2026-001',     'https://cdn.example.com/qr/CERT-SEC-YUSUF-2026-001.png',     DATEADD(day, -10, NOW()), FALSE),
-- Revoked certificate (issued in error)
(9, 7,  1, 'CERT-JAVA-BOB-2026-ERR',      'https://cdn.example.com/qr/CERT-JAVA-BOB-2026-ERR.png',      DATEADD(day, -30, NOW()), TRUE);

-- =========== NOTIFICATIONS ===========
-- Insert shared notifications (normalized schema: content lives here, links in employee_notifications)
INSERT INTO notifications (id, type, title, message, created_at) VALUES
(1,   'COURSE_ASSIGNED',     'New Course Available',       'You have been enrolled in Java Programming Fundamentals. Start learning today!',                     DATEADD(day, -25, NOW())),
(2,   'COURSE_ASSIGNED',     'New Course Available',       'Spring Boot Masterclass is now available for your department.',                                       DATEADD(day, -11, NOW())),
(3,   'COURSE_ASSIGNED',     'New Course Available',       'You have been enrolled in Java Programming Fundamentals. Start learning today!',                     DATEADD(day, -20, NOW())),
(4,   'COURSE_ASSIGNED',     'New Course Available',       'Java Programming Fundamentals is now assigned to your department.',                                   DATEADD(day, -20, NOW())),
(5,   'COURSE_ASSIGNED',     'New Course Available',       'Quality Assurance & Testing is ready. Complete all sections to earn your certificate.',              DATEADD(day, -25, NOW())),
(6,   'COURSE_ASSIGNED',     'New Course Available',       'You have been enrolled in Quality Assurance & Testing.',                                              DATEADD(day, -20, NOW())),
(7,   'COURSE_ASSIGNED',     'New Course Available',       'Quality Assurance & Testing has been assigned to your team.',                                         DATEADD(day, -20, NOW())),
(8,   'COURSE_ASSIGNED',     'New Course Available',       'You have been enrolled in Database Design with PostgreSQL.',                                          DATEADD(day, -28, NOW())),
(9,   'COURSE_ASSIGNED',     'New Course Available',       'Docker & Kubernetes Essentials has been assigned to your department.',                                DATEADD(day, -10, NOW())),
(10,  'COURSE_ASSIGNED',     'New Course Available',       'Docker & Kubernetes Essentials is now available for DevOps.',                                         DATEADD(day, -10, NOW())),
(11,  'COURSE_ASSIGNED',     'New Course Available',       'Data Analysis with Python is now available. Sharpen your analytics skills!',                         DATEADD(day, -35, NOW())),
(12,  'COURSE_ASSIGNED',     'New Course Available',       'Data Analysis with Python has been assigned to the Data & Analytics team.',                           DATEADD(day, -25, NOW())),
(13,  'COURSE_ASSIGNED',     'New Course Available',       'Agile & Scrum Methodology has been assigned. Complete it before June 30.',                           DATEADD(day, -10, NOW())),
(14,  'COURSE_ASSIGNED',     'New Course Available',       'Cybersecurity Fundamentals is mandatory for Legal & Compliance. Deadline: April 30.',                DATEADD(day, -15, NOW())),
(15,  'COURSE_ASSIGNED',     'New Course Available',       'Cybersecurity Fundamentals is mandatory for Security department. Deadline: April 1.',                DATEADD(day, -15, NOW())),
(16,  'DEADLINE_REMINDER',   'Course Assignment Reminder', 'Spring Boot Masterclass is due in 10 days. You are 50% through — keep going!',                       DATEADD(hour, -2, NOW())),
(17,  'DEADLINE_REMINDER',   'Course Assignment Reminder', 'Java Programming Fundamentals is due in 8 days. You have 3 of 6 sections remaining.',                DATEADD(hour, -4, NOW())),
(18,  'DEADLINE_REMINDER',   'Course Assignment Reminder', 'Docker & Kubernetes deadline is in 6 days. Complete sections 3 and 4 to finish.',                    DATEADD(hour, -1, NOW())),
(19,  'DEADLINE_REMINDER',   'Urgent: Course Not Started', 'Cybersecurity Fundamentals is due in 10 days and you have not started. Please begin immediately!',   DATEADD(minute, -30, NOW())),
(20,  'DEADLINE_REMINDER',   'Course Assignment Reminder', 'Agile & Scrum is due June 30. You have completed 1 of 4 sections.',                                  DATEADD(hour, -6, NOW())),
(21,  'DEADLINE_REMINDER',   'Course Assignment Reminder', 'Database Design with PostgreSQL deadline is approaching. You are on section 2 of 6.',                DATEADD(hour, -3, NOW())),
(22,  'DEADLINE_REMINDER',   'Course Assignment Reminder', 'Data Analysis with Python is due in 12 days. Keep up the momentum!',                                 DATEADD(hour, -5, NOW())),
(23,  'COURSE_OVERDUE',      'Congratulations!',           'You have successfully completed Java Programming Fundamentals with a score of 85%!',                 DATEADD(day, -12, NOW())),
(24,  'COURSE_OVERDUE',      'Congratulations!',           'Great work! You completed Java Programming Fundamentals with a score of 72%.',                       DATEADD(day, -8, NOW())),
(25,  'COURSE_OVERDUE',      'Congratulations!',           'Outstanding! You completed Quality Assurance & Testing with a score of 91%.',                        DATEADD(day, -12, NOW())),
(26,  'COURSE_OVERDUE',      'Congratulations!',           'Quality Assurance & Testing completed with a score of 80%. Well done, Grace!',                       DATEADD(day, -9, NOW())),
(27,  'COURSE_OVERDUE',      'Congratulations!',           'Quality Assurance & Testing completed with a score of 76%. Certificate issued!',                     DATEADD(day, -5, NOW())),
(28,  'COURSE_OVERDUE',      'Congratulations!',           'Database Design with PostgreSQL completed with a score of 88%! Excellent work.',                     DATEADD(day, -15, NOW())),
(29,  'COURSE_OVERDUE',      'Congratulations!',           'Data Analysis with Python completed. Score: 74%. Your certificate is ready!',                        DATEADD(day, -22, NOW())),
(30,  'COURSE_OVERDUE',      'Congratulations!',           'Cybersecurity Fundamentals completed with an outstanding score of 92%!',                             DATEADD(day, -10, NOW())),
(31,  'CERTIFICATE_READY',   'Certificate Ready',          'Your Java Programming Fundamentals certificate is ready. Download it from your profile.',            DATEADD(day, -12, NOW())),
(32,  'CERTIFICATE_READY',   'Certificate Ready',          'Your Java Programming Fundamentals certificate has been issued.',                                    DATEADD(day, -8, NOW())),
(33,  'CERTIFICATE_READY',   'Certificate Ready',          'Your QA & Testing certificate is ready for download. Share it on LinkedIn!',                        DATEADD(day, -12, NOW())),
(34,  'CERTIFICATE_READY',   'Certificate Ready',          'Your QA & Testing certificate has been issued.',                                                     DATEADD(day, -9, NOW())),
(35,  'CERTIFICATE_READY',   'Certificate Ready',          'Your QA & Testing certificate is available in your profile.',                                        DATEADD(day, -5, NOW())),
(36,  'CERTIFICATE_READY',   'Certificate Ready',          'Your Database Design certificate is ready. Share it on LinkedIn!',                                   DATEADD(day, -15, NOW())),
(37,  'CERTIFICATE_READY',   'Certificate Ready',          'Your Data Analysis with Python certificate has been issued.',                                        DATEADD(day, -22, NOW())),
(38,  'CERTIFICATE_READY',   'Certificate Ready',          'Your Cybersecurity Fundamentals certificate is ready for download.',                                 DATEADD(day, -10, NOW())),
(39,  'QCM_RESULT',          'Exam Results Available',     'Your Java Programming final exam results are available. You scored 85/100.',                         DATEADD(day, -12, NOW())),
(40,  'QCM_RESULT',          'Exam Results Available',     'Java Programming final exam results published. Your score: 55/100. One retry remaining.',            DATEADD(day, -10, NOW())),
(41,  'QCM_RESULT',          'Exam Results Available',     'Java Programming final exam: 72/100. Congratulations, you passed!',                                  DATEADD(day, -8, NOW())),
(42,  'QCM_RESULT',          'Exam Results Available',     'Java Programming final exam: 52/100. Maximum attempts reached. Please contact HR.',                  DATEADD(day, -18, NOW())),
(43,  'QCM_RESULT',          'Exam Results Available',     'QA & Testing final exam: 91/100. Outstanding performance!',                                          DATEADD(day, -12, NOW())),
(44,  'QCM_RESULT',          'Exam Results Available',     'Database Design final exam: 88/100. Results posted to your profile.',                                DATEADD(day, -15, NOW())),
(45,  'QCM_RESULT',          'Exam Results Available',     'Data Analysis final exam: 74/100. You passed. Certificate issued.',                                  DATEADD(day, -22, NOW())),
-- FIX: These 8 rows were previously malformed inside employee_notifications.
--      They belong in the notifications table and are inserted here correctly.
(46,  'QCM_RESULT',          'Exam Results Available',     'Cybersecurity final exam: 92/100. Exceptional result!',                                              DATEADD(day, -10, NOW())),
(47,  'SYSTEM_ALERT',        'New Learning Resources Available','Advanced Java video tutorials are now in the course library. Check them out!',                  DATEADD(hour, -8, NOW())),
(48,  'SYSTEM_ALERT',        'Platform Maintenance Scheduled',  'System maintenance on March 25, 2026 from 02:00-04:00 UTC. Service may be briefly unavailable.',DATEADD(hour, -6, NOW())),
(49,  'SYSTEM_ALERT',        'Q&A Session Coming Up',           'Live Q&A on testing best practices this Friday at 2 PM. Link in Slack #training.',              DATEADD(hour, -2, NOW())),
(50,  'SYSTEM_ALERT',        'Kubernetes v1.30 Release',        'Kubernetes 1.30 is out. Check the release notes and update your course knowledge.',              DATEADD(day, -1, NOW())),
(51,  'SYSTEM_ALERT',        'Mandatory Security Training',     'Cybersecurity Fundamentals is mandatory for Security dept. Deadline: April 1. Please start now.',DATEADD(day, -3, NOW())),
(52,  'SYSTEM_ALERT',        'Data Team Workshop',              'Monthly data workshop scheduled for March 28. Attendance required for all analytics staff.',     DATEADD(hour, -12, NOW())),
(53,  'SYSTEM_ALERT',        'GDPR Course Coming Soon',         'GDPR & Data Privacy Compliance course launches next quarter. Legal team will be first enrolled.', DATEADD(day, -2, NOW()));

-- Link notifications to employees (employee_notifications join table)
-- Columns: (employee_id, notification_id, is_read, created_at)
INSERT INTO employee_notifications (employee_id, notification_id, is_read, created_at) VALUES
(6,   1,  FALSE, DATEADD(day, -25, NOW())),
(6,   2,  FALSE, DATEADD(day, -11, NOW())),
(7,   3,  FALSE, DATEADD(day, -20, NOW())),
(8,   4,  FALSE, DATEADD(day, -20, NOW())),
(12,  5,  FALSE, DATEADD(day, -25, NOW())),
(13,  6,  FALSE, DATEADD(day, -20, NOW())),
(14,  7,  FALSE, DATEADD(day, -20, NOW())),
(9,   8,  FALSE, DATEADD(day, -28, NOW())),
(15,  9,  FALSE, DATEADD(day, -10, NOW())),
(16,  10, FALSE, DATEADD(day, -10, NOW())),
(33,  11, FALSE, DATEADD(day, -35, NOW())),
(34,  12, FALSE, DATEADD(day, -25, NOW())),
(18,  13, FALSE, DATEADD(day, -10, NOW())),
(31,  14, FALSE, DATEADD(day, -15, NOW())),
(41,  15, FALSE, DATEADD(day, -15, NOW())),
(6,   16, FALSE, DATEADD(hour, -2, NOW())),
(7,   17, FALSE, DATEADD(hour, -4, NOW())),
(15,  18, FALSE, DATEADD(hour, -1, NOW())),
(41,  19, FALSE, DATEADD(minute, -30, NOW())),
(18,  20, FALSE, DATEADD(hour, -6, NOW())),
(11,  21, FALSE, DATEADD(hour, -3, NOW())),
(34,  22, FALSE, DATEADD(hour, -5, NOW())),
(6,   23, TRUE,  DATEADD(day, -12, NOW())),
(8,   24, TRUE,  DATEADD(day, -8, NOW())),
(12,  25, TRUE,  DATEADD(day, -12, NOW())),
(13,  26, TRUE,  DATEADD(day, -9, NOW())),
(14,  27, TRUE,  DATEADD(day, -5, NOW())),
(9,   28, TRUE,  DATEADD(day, -15, NOW())),
(33,  29, TRUE,  DATEADD(day, -22, NOW())),
(31,  30, TRUE,  DATEADD(day, -10, NOW())),
(6,   31, TRUE,  DATEADD(day, -12, NOW())),
(8,   32, TRUE,  DATEADD(day, -8, NOW())),
(12,  33, TRUE,  DATEADD(day, -12, NOW())),
(13,  34, TRUE,  DATEADD(day, -9, NOW())),
(14,  35, TRUE,  DATEADD(day, -5, NOW())),
(9,   36, TRUE,  DATEADD(day, -15, NOW())),
(33,  37, TRUE,  DATEADD(day, -22, NOW())),
(31,  38, TRUE,  DATEADD(day, -10, NOW())),
(6,   39, TRUE,  DATEADD(day, -12, NOW())),
(7,   40, FALSE, DATEADD(day, -10, NOW())),
(8,   41, TRUE,  DATEADD(day, -8, NOW())),
(10,  42, FALSE, DATEADD(day, -18, NOW())),
(12,  43, TRUE,  DATEADD(day, -12, NOW())),
(9,   44, TRUE,  DATEADD(day, -15, NOW())),
(33,  45, TRUE,  DATEADD(day, -22, NOW())),
-- FIX: These rows now correctly reference notifications 46-53 defined above.
(31,  46, TRUE,  DATEADD(day, -10, NOW())),
(6,   47, FALSE, DATEADD(hour, -8, NOW())),
(7,   48, FALSE, DATEADD(hour, -6, NOW())),
(12,  49, FALSE, DATEADD(hour, -2, NOW())),
(15,  50, FALSE, DATEADD(day, -1, NOW())),
(41,  51, FALSE, DATEADD(day, -3, NOW())),
(33,  52, FALSE, DATEADD(hour, -12, NOW())),
(31,  53, FALSE, DATEADD(day, -2, NOW()));

-- =========== AUDIT LOGS ===========
INSERT INTO audit_logs (id, user_id, action_type, entity_type, entity_id, timestamp, old_value, new_value) VALUES
-- Course lifecycle
(1,  1, 'CREATE',   'Course',      1,  DATEADD(day, -60, NOW()),  NULL,                                                        '{"title":"Java Programming Fundamentals","status":"DRAFT"}'),
(2,  1, 'UPDATE',   'Course',      1,  DATEADD(day, -55, NOW()),  '{"status":"DRAFT"}',                                        '{"status":"PUBLISHED"}'),
(3,  1, 'CREATE',   'Course',      2,  DATEADD(day, -58, NOW()),  NULL,                                                        '{"title":"Spring Boot Masterclass","status":"DRAFT"}'),
(4,  1, 'UPDATE',   'Course',      2,  DATEADD(day, -50, NOW()),  '{"status":"DRAFT"}',                                        '{"status":"PUBLISHED"}'),
(5,  2, 'CREATE',   'Course',      6,  DATEADD(day, -45, NOW()),  NULL,                                                        '{"title":"Docker & Kubernetes Essentials","status":"DRAFT"}'),
(6,  2, 'UPDATE',   'Course',      6,  DATEADD(day, -40, NOW()),  '{"status":"DRAFT"}',                                        '{"status":"PUBLISHED"}'),
(7,  3, 'CREATE',   'Course',      4,  DATEADD(day, -50, NOW()),  NULL,                                                        '{"title":"Quality Assurance & Testing","status":"DRAFT"}'),
(8,  3, 'UPDATE',   'Course',      4,  DATEADD(day, -44, NOW()),  '{"status":"DRAFT"}',                                        '{"status":"PUBLISHED"}'),
(9,  5, 'CREATE',   'Course',      9,  DATEADD(day, -35, NOW()),  NULL,                                                        '{"title":"Agile & Scrum Methodology","status":"DRAFT"}'),
(10, 5, 'UPDATE',   'Course',      9,  DATEADD(day, -30, NOW()),  '{"status":"DRAFT"}',                                        '{"status":"PUBLISHED"}'),
(11, 1, 'UPDATE',   'Course',      13, DATEADD(day, -90, NOW()),  '{"status":"PUBLISHED","deleted":false}',                    '{"status":"ARCHIVED","deleted":true}'),
-- Employee management
(12, 4, 'CREATE',   'Employee',    28, DATEADD(day, -90, NOW()),  NULL,                                                        '{"email":"vera.lindqvist@example.com","role":"ROLE_EMPLOYEE"}'),
(13, 4, 'CREATE',   'Employee',    29, DATEADD(day, -88, NOW()),  NULL,                                                        '{"email":"william.tan@example.com","role":"ROLE_EMPLOYEE"}'),
(14, 4, 'CREATE',   'Employee',    41, DATEADD(day, -120, NOW()), NULL,                                                        '{"email":"igor.volkov@example.com","department_id":18}'),
(15, 1, 'DELETE',   'Employee',    43, DATEADD(day, -10, NOW()),  '{"email":"karl.deleted@example.com","deleted":false}',      '{"deleted":true}'),
(16, 1, 'DELETE',   'Employee',    44, DATEADD(day, -5, NOW()),   '{"email":"lena.deleted@example.com","deleted":false}',      '{"deleted":true}'),
-- Certificate revocation
(17, 1, 'REVOKE',   'Certificate', 9,  DATEADD(day, -28, NOW()),  '{"employee_id":7,"revoked":false}',                         '{"revoked":true,"reason":"Issued in error - employee had not completed all sections"}'),
-- Grade override
(18, 2, 'OVERRIDE', 'CourseGrade', 10, DATEADD(day, -7, NOW()),   '{"employee_id":10,"final_score":52}',                       '{"employee_id":10,"final_score":52,"note":"Max attempts reached, no re-sit permitted per policy"}'),
-- Department restructuring
(19, 1, 'CREATE',   'Department',  18, DATEADD(day, -120, NOW()), NULL,                                                        '{"name":"Security","parent_department_id":1}'),
(20, 1, 'UPDATE',   'Department',  15, DATEADD(day, -100, NOW()), '{"name":"Data Science"}',                                   '{"name":"Data & Analytics"}'),
(21, 1, 'CREATE',   'Department',  4,  DATEADD(day, -150, NOW()), NULL,                                                        '{"name":"DevOps & Infrastructure","parent_department_id":1}'),
-- Course section edits
(22, 2, 'UPDATE',   'CourseSection',9, DATEADD(day, -35, NOW()),  '{"title":"Authentication in Spring"}',                      '{"title":"Security with Spring Security"}'),
(23, 3, 'CREATE',   'CourseSection',23,DATEADD(day, -44, NOW()),  NULL,                                                        '{"title":"API Testing with REST Assured","course_id":4}');