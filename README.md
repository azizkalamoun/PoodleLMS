# Poodle LMS

**Internal Corporate Training & Learning Management System**

Poodle LMS is an enterprise-grade learning management system for corporate training. It enables administrators to create courses with mixed media content (video, PDF, text, QCM), assign them to departments with deadlines, and track employee progress through completion and QCM-based assessments. Employees receive certificates upon passing final exams, and the system provides analytics dashboards for completion rates, pass rates, overdue enrollments, and department performance.

The system comprises two independently deployable applications:
- **Backend**: Spring Boot 3.4.3 REST API (Java 21)
- **Frontend**: Angular 17.3 single-page application (standalone components)

---

## Architecture Overview

### Separation of Concerns

- **Backend** (`backend_springboot/`): Spring Boot application exposing a JSON REST API on port `8081`. Handles authentication, business logic, persistence (JPA/Hibernate), Flyway-managed schema migrations, LLM-based QCM draft generation, file uploads (Cloudinary), PDF text extraction (PDFBox), and certificate issuance.
- **Frontend** (`frontend_angular/`): Angular 17.3 SPA consuming the REST API. Built with standalone components, Angular Material, Tailwind CSS, and Chart.js. Served during development on port `4200`.

### Communication

- **REST API**: All data exchange uses JSON over HTTP. The frontend communicates with the backend exclusively through REST endpoints under `/api/`.
- **Authentication**: JWT bearer tokens issued at `/api/auth/login`. The token is stored in `localStorage` and attached to every request via an HTTP interceptor.
- **No WebSocket or server-sent events** are used.

### Deployment Structure

The project is a **monorepo** with two sibling directories:

```
PoodleLMS/
├── backend_springboot/    # Spring Boot application
│   ├── Dockerfile         # Containerized deployment
│   └── src/
└── frontend_angular/      # Angular application
    ├── angular.json
    └── src/
```

Production frontend is built statically and served via a CDN or web server; the production `apiUrl` points to a deployed backend at `https://backend-springboot-i7r7.onrender.com/api`.

### Data Flow

1. User authenticates → Angular sends `POST /api/auth/login` → backend validates credentials → returns JWT + user profile.
2. Angular stores JWT in `localStorage`, attaches it via `jwtInterceptor`.
3. Subsequent requests reach `SecurityConfig` → `JwtAuthenticationFilter` extracts and validates the token, sets `SecurityContext`.
4. Controllers receive authenticated requests, delegate to service layer, which uses Spring Data JPA repositories.
5. Responses are serialized to JSON (Jackson with `JavaTimeModule` and `NON_NULL` inclusion).
6. The frontend updates its RxJS `BehaviorSubject`-based state and re-renders Angular components.

### Authentication Flow

1. `POST /api/auth/login` with `LoginRequest` (email, password).
2. Backend validates credentials via `AuthenticationManager` → `CustomUserDetailsService.loadUserByUsername()`.
3. On success, `JwtTokenProvider.generateToken()` creates a JWT signed with HMAC-SHA256.
4. Response includes `{ token, email, firstName, lastName, role }`.
5. Angular `AuthService.login()` stores token + user in `localStorage`.
6. `jwtInterceptor` automatically attaches `Authorization: Bearer <token>` to all outgoing HTTP requests.
7. On 401 responses, the interceptor calls `AuthService.logout()` to clear session and redirect to `/login`.
8. Public endpoints: `/api/auth/**`, `/api/certificates/verify/**`, Swagger UI, H2 console.

### CORS

Configured in `SecurityConfig.corsConfigurationSource()`. Allowed origins are read from `app.cors.allowed-origins` (default: `http://localhost:3000,http://localhost:4200`). All methods (GET, POST, PUT, DELETE, OPTIONS) and all headers are permitted. Credentials are allowed. Preflight cache max age is 3600 seconds.

### Environment Configuration

**Backend** uses Spring profiles (`dev` / default) with:
- `application.yml`: PostgreSQL config, JWT secret, CORS origins, LLM API key, logging levels.
- `application-dev.yml`: H2 in-memory database (PostgreSQL compatibility mode), dev-specific logging.
- `.env` file loaded via `spring-dotenv` for local secrets.
- All sensitive values overridable via environment variables (`DB_URL`, `JWT_SECRET`, `GEMINI_API_KEY`, etc.).

**Frontend** uses Angular environments:
- `environment.ts`: `apiUrl = 'http://localhost:8081/api'` (development).
- `environment.prod.ts`: `apiUrl = 'https://backend-springboot-i7r7.onrender.com/api'` (production).

---

## Key Features

| Feature | Description | Backend | Frontend |
|---|---|---|---|
| **Authentication** | Email/password login, JWT-based session | `AuthController`, `AuthService`, `JwtTokenProvider` | `AuthService`, `login.component` |
| **Role-based Access** | ADMIN and EMPLOYEE roles, method-level security | `@PreAuthorize`, `SecurityConfig` | `roleGuard`, route guards |
| **Course Management** | CRUD for courses with status (DRAFT/PUBLISHED/ARCHIVED) | `CourseController`, `CourseService` | `admin-courses.component`, `admin-course-detail.component` |
| **Department Hierarchy** | Tree-structured departments with parent-child relationships | `DepartmentController`, `DepartmentService` | `admin-departments.component` |
| **Course-Department Assignment** | Assign courses to departments with deadlines | `CourseController.assignCourseToDepartment` | `CourseService.assignToDepartment` |
| **Course Sections** | Mixed media content (VIDEO, PDF, TEXT, IMAGE, QCM) per course | `SectionController`, `SectionService` | `admin-course-detail.component` |
| **QCM Assessments** | Practice and Final exam sections with multiple attempts | `QCMController`, `QCMService` | `employee-course-detail.component` |
| **AI-generated QCM Drafts** | LLM-powered (Google Gemini) question generation from PDF content | `LLMService`, `PDFReaderService` | `CourseService.generateQcmDraft` |
| **Employee Progress Tracking** | Per-section completion tracking per employee per course | `ProgressController`, `ProgressService` | `employee-course-detail.component`, `employee-progress.component` |
| **Course Grades & Certificates** | Auto-graded final exams, certificate issuance with QR codes | `CertificateController`, `CertificateService` | `employee-certificates.component`, `admin-certificates.component` |
| **Certificate Verification** | Public certificate verification via unique code | `CertificateController.verifyCertificate` | `certificate-verify.component` (public) |
| **Notifications** | Course assignment, deadline reminders, QCM results | `NotificationController`, `NotificationService` | `notification.service`, `notification-bell.component`, `notification-modal.component` |
| **Analytics Dashboard** | Completion rates, pass rates, overdue employees, failed questions | `AnalyticsController`, `AnalyticsService` | `admin-analytics.component` (Chart.js) |
| **Audit Logging** | Track CREATE/UPDATE/DELETE on entities | `AuditLogController`, `AuditLogService` | Admin-facing |
| **File Upload** | Course section media uploads via Cloudinary | Cloudinary integration in `SectionService` | `cloudinary-upload.service`, `chunked-upload.service` |

---

## Backend Design (Spring Boot)

### Controller Layer

12 REST controllers at `com.enterprise.poodle.controller`:

| Controller | Base Path | Role Access |
|---|---|---|
| `AuthController` | `/api/auth` | Public (login), ADMIN (register) |
| `CourseController` | `/api/courses` | Authenticated (GET), ADMIN (POST/PUT/DELETE) |
| `DepartmentController` | `/api/departments` | ADMIN |
| `EmployeeController` | `/api/employees` | Authenticated (self), ADMIN (all) |
| `SectionController` | `/api/sections` | ADMIN (write), Authenticated (read) |
| `QCMController` | `/api/sections/{id}/questions` | ADMIN |
| `EnrollmentController` | `/api/enrollments` | Authenticated |
| `ProgressController` | `/api/progress` | Authenticated |
| `NotificationController` | `/api/notifications` | Authenticated |
| `CertificateController` | `/api/certificates` | Public (verify), EMPLOYEE (my), ADMIN (all) |
| `AnalyticsController` | `/api/analytics` | ADMIN |
| `AuditLogController` | `/api/audit-logs` | ADMIN |

All controllers use `@RestController` and `@RequestMapping`. Method-level security is enforced with `@PreAuthorize("hasRole('ADMIN')")`.

### Service Layer

14 service classes encapsulate business logic:

- `AuthService` – Login (delegates to `AuthenticationManager`), registration (password hashing, employee creation with role validation).
- `CourseService` – CRUD, department assignment/unassignment with deadline, prerequisite management, publish notifications.
- `SectionService` – CRUD for course sections, file upload to Cloudinary.
- `QCMService` – Question CRUD, attempt submission with auto-grading, attempt count validation.
- `EnrollmentService` – Enrollment lookup by employee/course, QCM attempt submission (practice/final), course grade and certificate generation on passing.
- `ProgressService` – Section completion tracking, course-level progress aggregation.
- `CertificateService` – Certificate issuance with UUID code, revocation, verification, QR code URL generation, paginated listing.
- `NotificationService` – Notification creation, read/unread tracking, bulk mark-read, deletion, notification for course assignment/overdue/QCM results.
- `AnalyticsService` – Dashboard metrics: completion rate, pass rate, department average score, overdue employees, most-failed questions. Uses multiple repositories with `@Transactional(readOnly = true)`.
- `EmployeeService` – Profile management, password change, admin CRUD with email uniqueness validation.
- `DepartmentService` – CRUD, tree structure building (parent-child relationships).
- `AuditLogService` – Paginated querying by entity or user.
- `LLMService` – AI QCM draft generation via Google Gemini API (REST), PDF content extraction via PDFBox, fallback to placeholder questions when API key is absent.
- `PDFReaderService` – Fetches and caches PDF content from URLs for LLM context.

### Repository Layer

15 Spring Data JPA repositories. Notable patterns:
- Custom `@Query` methods for analytics aggregates (e.g., `findAverageScoreByDepartmentId`, `countPassedByCourseId`).
- `@Modifying` + `@Transactional` for bulk updates (`markAsRead`, `markAllAsRead`).
- Soft-delete filtering (`deleted = false`) in queries.
- `@EntityGraph` / `LEFT JOIN FETCH` in `CertificateRepository` for eager loading.

### DTO Layer

- **Request DTOs** (`dto/request/`): 12 classes annotated with `@Valid` / Jakarta Bean Validation (`@NotBlank`, `@Email`, `@Size`). Include `LoginRequest`, `RegisterRequest`, `CourseRequest`, `QCMAttemptRequest`, `GenerateQCMDraftRequest`, etc.
- **Response DTOs** (`dto/response/`): 20 classes built with Lombok `@Builder`. Include `AuthResponse`, `CourseResponse`, `QCMAttemptResponse`, `AnalyticsResponse` (with nested inner classes), `ApiResponse<T>` for generic wrapping.

### Validation

Jakarta Bean Validation (`spring-boot-starter-validation`) on all request DTOs. Input validation is centralized at the controller level with `@Valid`. Custom business validation occurs in service methods (e.g., email uniqueness, role hierarchy, attempt limits).

### Security

`SecurityConfig` (`config/SecurityConfig.java`):
- CSRF disabled (stateless JWT).
- CORS configured from property.
- Session creation policy: `STATELESS`.
- Public endpoints: `/api/auth/**`, `/api/certificates/verify/**`, Swagger UI, H2 console, `OPTIONS`.
- All other requests require authentication.
- Custom 401 JSON response via `AuthenticationEntryPoint`.
- `JwtAuthenticationFilter` (extends `OncePerRequestFilter`) runs before `UsernamePasswordAuthenticationFilter`.

`JwtTokenProvider`:
- Generates HMAC-SHA256 signed JWTs via `jjwt` 0.12.6.
- Configurable secret and expiration (`app.jwt.expiration-ms`, default 24h).
- Validates signature and expiry on each request.

`SecurityUtils`:
- Provides `getCurrentEmployee()`, `getCurrentEmployeeId()`, `isAdmin()` helpers using `SecurityContextHolder`.

### Exception Handling

`GlobalExceptionHandler` (`@ControllerAdvice`):
- `ResourceNotFoundException` → 404
- `BusinessException` → 400
- `DuplicateResourceException` → 409
- `AccessDeniedException` → 403
- `MethodArgumentNotValidException` → 400 (validation errors)
- `AccessDeniedException` (Spring Security) → 403
- `GenericException` → 500

All exceptions return `ApiErrorResponse` with status, message, path, and timestamp.

### Transaction Management

- `@Transactional(readOnly = true)` on all service query methods.
- `@Transactional` on mutation methods.
- `open-in-view: false` to avoid lazy-loading issues in views.

### Database Integration

- **Primary**: PostgreSQL via JDBC with HikariCP connection pool (20 max, 5 min idle).
- **Dev**: H2 in-memory with `MODE=PostgreSQL` for compatibility.
- **JPA/Hibernate**: `ddl-auto: none` — Flyway manages schema. Batch inserts enabled (`batch_size: 30`, `order_inserts: true`).
- **Flyway**: 5 migration scripts under `src/main/resources/db/migration/`:
  - `V1__init_schema.sql`: Core tables (employees, courses, departments, sections, QCM questions, progress, attempts, grades, certificates, notifications, audit logs).
  - `V2__seed_test_data.sql`: Initial test data.
  - `V3__add_attempt_answers_table.sql`: Per-answer tracking for QCM attempts.
  - `V4__add_image_content_type.sql`: IMAGE content type support.
  - `V5__reset_sequences_h2.sql`: H2 sequence reset.
- Dev data seeding via `DevDataInitializer` (profile `dev`), using `CommandLineRunner`.

### API Versioning

No explicit API versioning strategy is used. All endpoints are under `/api/`.

### LLM Integration

`LLMService` integrates with Google Gemini API for automated QCM draft generation:
- Reads PDF content from a section's `contentUrl` using `PDFBox`.
- Sends the extracted text to Gemini via REST with a structured prompt requesting 4-option multiple-choice questions.
- Parses the JSON response and persists generated questions.
- Falls back to placeholder questions when API key is not configured.

---

## Frontend Design (Angular)

### Module Structure

The application uses **Angular standalone components** (no `NgModule`). All components, directives, and pipes are declared standalone via `@Component({ standalone: true })`.

Bootstrap is in `main.ts` using `bootstrapApplication(AppComponent, appConfig)`.

### Routing and Guards

```typescript
// app.routes.ts
{ path: 'login', component: LoginComponent }                       // public
{ path: 'verify/:code', component: CertificateVerifyComponent }    // public
{ path: 'admin', canActivate: [authGuard, roleGuard('ADMIN')],
  loadChildren: () => import('./admin/admin.routes') }             // lazy-loaded
{ path: 'employee', canActivate: [authGuard, roleGuard('EMPLOYEE')],
  loadChildren: () => import('./employee/employee.routes') }       // lazy-loaded
```

- `authGuard`: Checks `AuthService.isLoggedIn()`, redirects to `/login` if not.
- `roleGuard(role)`: Checks `AuthService.hasRole('ROLE_' + role)`, redirects to appropriate dashboard (admin or employee) if wrong role.
- Admin routes (4 lazy-loaded components): dashboard, employees, departments, courses (list + detail), certificates, analytics.
- Employee routes (4 lazy-loaded components): dashboard, courses (list + detail), certificates.

### Components Architecture

**Shared components** (`shared/components/`):
- `status-badge` — Colored badge for course/section status.
- `skeleton` — Loading skeleton placeholder.
- `empty-state` — Empty state illustration with message.
- `loading-spinner` — Full-page spinner.
- `page-header` — Consistent page header with title and optional actions.
- `confirm-dialog` — Reusable confirmation dialog (Angular Material).
- `form-dialog` — Dynamic form dialog for creating/editing entities.
- `notification-bell` — Notification icon with unread count badge.
- `notification-modal` — Notification list drawer.
- `qr-code` — QR code display for certificates.
- `certificate-verify` — Public certificate verification page.

**Layout components**:
- `AdminLayoutComponent` — Sidebar + top navbar for admin.
- `EmployeeLayoutComponent` — Simplified layout for employees.

**Feature components** are organized under `admin/` and `employee/`.

### Services and Dependency Injection

Services are provided with `providedIn: 'root'`. Key services:

| Service | Responsibility |
|---|---|
| `AuthService` | Login/logout, token storage (localStorage), user state (`BehaviorSubject`), role checks |
| `ApiService` | HTTP wrapper for GET/POST/PUT/DELETE/PATCH with centralized error handling |
| `CourseService` | Course CRUD, sections, questions, assignments, QCM draft generation |
| `EmployeeService` | Employee CRUD, department filter |
| `DepartmentService` | Department CRUD, tree structure |
| `EnrollmentService` | Enrollment lookup, practice/final attempt submission |
| `CertificateService` | Certificate listing (employee + admin), verification, revocation |
| `NotificationService` | Notification CRUD, unread count, optimistic UI updates |
| `AnalyticsService` | All analytics endpoints |
| `CloudinaryUploadService` | File upload via Cloudinary signed upload |
| `CloudinaryAssetService` | Cloudinary asset management |
| `ChunkedUploadService` | Large file chunked upload to Cloudinary |
| `SnackbarService` | Material snackbar wrapper |
| `ToastService` | Toast notification utility |

### State Management

State is managed with **RxJS `BehaviorSubject`** and `Observable`:
- `AuthService.currentUser$` — Current authenticated user.
- `NotificationService.notifications$` / `unreadCount$` — Notifications with optimistic updates and rollback on error.

No dedicated state management library (NgRx, Signals) is used.

### Forms

**Reactive Forms** are used throughout. Pattern: `FormBuilder` + `Validators` in component constructors, binding via `[formGroup]`.

### HTTP Client Usage

- `provideHttpClient(withInterceptors([jwtInterceptor]))` in `app.config.ts`.
- `jwtInterceptor` attaches `Authorization: Bearer <token>` header from `localStorage`.
- On 401 responses: logs out if token exists and not already on `/login`.
- `ApiService` provides a centralized wrapper with HTTP method helpers and error parsing.

### Interceptors

One functional interceptor: `jwtInterceptor` (HTTP interceptor function, `HttpInterceptorFn`). Handles token attachment and 401 logout.

### UI Structure

- **Tailwind CSS** for utility-first styling with custom design tokens (brand teal color `#3b9ca2`, surface colors, shadows, animations).
- **Angular Material** components: cards, tables, dialogs, snackbars, tabs, expansion panels, paginator, menus, tooltips, form fields, buttons, icons.
- **Custom design token system**: CSS custom properties for brand, surface, border, text colors; dark mode support (`.dark` class).
- **Chart.js** via `ng2-charts` for analytics visualizations.
- **SCSS** styles with `@angular/material/prebuilt-themes/indigo-pink.css`.

### Performance Optimizations

- Lazy-loaded admin and employee route modules.
- `outputHashing: 'all'` in production builds for cache busting.
- Budget warnings: initial JS < 1 MB (warning) / < 2 MB (error); component styles < 6 KB (warning) / < 10 KB (error).

---

## API Design and Data Flow

### REST Endpoints

#### Authentication
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/login` | Public | Login, returns JWT |
| POST | `/api/auth/register` | ADMIN | Create employee |

#### Courses
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/courses` | Auth | List courses (paginated, sortable) |
| POST | `/api/courses` | ADMIN | Create course |
| GET | `/api/courses/{id}` | Auth | Get course by ID |
| PUT | `/api/courses/{id}` | ADMIN | Update course |
| DELETE | `/api/courses/{id}` | ADMIN | Soft-delete course |
| POST | `/api/courses/{courseId}/assign/{deptId}` | ADMIN | Assign to department |
| GET | `/api/courses/{courseId}/assigned-departments` | ADMIN | Get assignments |
| DELETE | `/api/courses/{courseId}/assign/{deptId}` | ADMIN | Unassign |

#### Employees
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/employees/me` | Auth | Current profile |
| PUT | `/api/employees/me` | Auth | Update own profile |
| PUT | `/api/employees/me/password` | Auth | Change password |
| GET | `/api/employees` | ADMIN | List all (paginated) |
| POST | `/api/employees` | ADMIN | Create employee |
| GET | `/api/employees/{id}` | ADMIN | Get by ID |
| PUT | `/api/employees/{id}` | ADMIN | Update employee |
| DELETE | `/api/employees/{id}` | ADMIN | Delete (soft) |

#### Departments
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/departments` | ADMIN | List (paginated) |
| GET | `/api/departments/tree` | ADMIN | Tree structure |
| POST | `/api/departments` | ADMIN | Create |
| PUT | `/api/departments/{id}` | ADMIN | Update |
| DELETE | `/api/departments/{id}` | ADMIN | Delete |

#### Sections
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/courses/{id}/sections` | Auth | List sections by course |
| POST | `/api/courses/{id}/sections` | ADMIN | Create section |
| GET | `/api/sections/{id}` | Auth | Get section |
| PUT | `/api/sections/{id}` | ADMIN | Update section |
| DELETE | `/api/sections/{id}` | ADMIN | Delete section |

#### QCM Questions
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/sections/{id}/questions` | Auth | List questions |
| POST | `/api/sections/{id}/questions` | ADMIN | Create question |
| PUT | `/api/sections/{id}/questions/{qId}` | ADMIN | Update question |
| DELETE | `/api/sections/{id}/questions/{qId}` | ADMIN | Delete question |
| POST | `/api/courses/{id}/sections/{sId}/generate-qcm` | ADMIN | AI-generate QCM draft |

#### QCM Attempts
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/enrollments/{id}/practice-attempt` | Auth | Submit practice attempt |
| POST | `/api/enrollments/{id}/final-attempt` | Auth | Submit final attempt |
| GET | `/api/sections/{id}/my-attempt-count` | Auth | Get attempt count |

#### Progress
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/progress/my-courses` | Auth | Employee's course list |
| GET | `/api/progress/course/{courseId}` | Auth | Course progress detail |
| GET | `/api/progress/employee/{empId}` | ADMIN | Employee's all progress |

#### Certificates
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/certificates/my` | EMPLOYEE | My certificates (paginated) |
| GET | `/api/certificates` | ADMIN | All certificates |
| GET | `/api/certificates/verify/{code}` | Public | Verify certificate |
| PUT | `/api/certificates/{id}/revoke` | ADMIN | Revoke |
| PUT | `/api/certificates/{id}/unrevoke` | ADMIN | Un-revoke |

#### Notifications
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/notifications/me` | Auth | My notifications |
| GET | `/api/notifications/me/unread` | Auth | Unread notifications |
| GET | `/api/notifications/me/unread-count` | Auth | Unread count |
| PUT | `/api/notifications/{id}/read` | Auth | Mark read |
| PUT | `/api/notifications/read-all` | Auth | Mark all read |
| DELETE | `/api/notifications/{id}` | Auth | Delete |

#### Analytics
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/analytics/overview` | ADMIN | Aggregate KPIs |
| GET | `/api/analytics/courses` | ADMIN | Per-course analytics |
| GET | `/api/analytics/overdue` | ADMIN | Overdue enrollments |
| GET | `/api/analytics/failed-questions` | ADMIN | Most failed questions |
| GET | `/api/analytics/departments` | ADMIN | Per-department analytics |
| GET | `/api/analytics/course/{id}/completion` | ADMIN | Course completion rate |
| GET | `/api/analytics/course/{id}/pass-rate` | ADMIN | Course pass rate |
| GET | `/api/analytics/course/{id}/overdue` | ADMIN | Overdue employees |
| GET | `/api/analytics/course/{id}/most-failed-questions` | ADMIN | Per-course failed Qs |
| GET | `/api/analytics/department/{id}/average-score` | ADMIN | Dept average score |

#### Audit Logs
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/audit-logs` | ADMIN | All logs (paginated) |
| GET | `/api/audit-logs/entity/{type}/{id}` | ADMIN | By entity |
| GET | `/api/audit-logs/user/{id}` | ADMIN | By user |

### Pagination

Spring Boot returns Spring Data `Page<T>` responses. All list endpoints accept `page`, `size`, and `sort` query parameters. The frontend extracts `.content` from the page envelope.

### Error Handling Strategy

**Backend**: `GlobalExceptionHandler` returns structured JSON errors:
```json
{ "status": 400, "message": "...", "path": "/api/...", "timestamp": "..." }
```

**Frontend**: `ApiService.handleError` parses server errors and maps HTTP status codes to user-facing messages. The `jwtInterceptor` handles 401 by clearing session. Components subscribe to service errors for local notification display.

### Authentication and Authorization Flow

1. User enters credentials → `LoginComponent` calls `AuthService.login()`.
2. Backend validates via `AuthenticationManager` + `CustomUserDetailsService`.
3. On success: JWT is generated, response includes token + user fields.
4. `AuthService` stores token and user in `localStorage`, emits via `currentUser$`.
5. Router redirects based on role (`isAdmin()` → `/admin/dashboard`, else `/employee/dashboard`).
6. All subsequent API calls include `Authorization: Bearer <token>` via `jwtInterceptor`.
7. Backend `JwtAuthenticationFilter` validates token on every request.
8. On token expiry (401), interceptor logs out and redirects to `/login`.

---

## Technologies and Dependencies

### Backend (`pom.xml`)

| Dependency | Version | Purpose |
|---|---|---|
| [Spring Boot Starter Web](https://spring.io/projects/spring-boot) | 3.4.3 | REST API, embedded Tomcat |
| [Spring Boot Starter Data JPA](https://spring.io/projects/spring-data-jpa) | 3.4.3 | Hibernate ORM, repository abstraction |
| [Spring Boot Starter Security](https://spring.io/projects/spring-security) | 3.4.3 | Authentication, authorization, method security |
| [Spring Boot Starter Validation](https://spring.io/projects/spring-boot) | 3.4.3 | Jakarta Bean Validation (`@Valid`) |
| [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/) | 42.x | Production database connectivity |
| [H2 Database](https://www.h2database.com/) | 2.x | In-memory dev database (PostgreSQL compatibility mode) |
| [JJWT](https://github.com/jwtk/jjwt) | 0.12.6 | JWT creation and validation (HMAC-SHA256) |
| [Lombok](https://projectlombok.org/) | 1.18.34 | `@Data`, `@Builder`, `@RequiredArgsConstructor`, etc. |
| [MapStruct](https://mapstruct.org/) | 1.6.3 | Entity-to-DTO mapping (compile-time code generation) |
| [SpringDoc OpenAPI](https://springdoc.org/) | 2.8.4 | Swagger UI at `/swagger-ui.html`, OpenAPI spec at `/v3/api-docs` |
| [spring-dotenv](https://github.com/paulschwarz/spring-dotenv) | 4.0.0 | `.env` file loading for local secrets |
| [Flyway](https://flywaydb.org/) | 10.x | Database schema migrations |
| [Cloudinary HTTP44](https://cloudinary.com/documentation/java_integration) | 1.38.0 | File upload to Cloudinary CDN |
| [Apache PDFBox](https://pdfbox.apache.org/) | 2.0.32 | PDF text extraction for LLM context |
| [Spring Boot Starter Test](https://spring.io/projects/spring-boot) | 3.4.3 | JUnit 5, Mockito, `@WebMvcTest`, `@DataJpaTest` |
| [Spring Security Test](https://spring.io/projects/spring-security) | 3.4.3 | Security test utilities, `@WithMockUser` |

### Frontend (`package.json`)

| Dependency | Version | Purpose |
|---|---|---|
| [@angular/core](https://angular.io/) | ^17.3.0 | Framework (standalone components) |
| [@angular/router](https://angular.io/guide/router) | ^17.3.0 | Client-side routing, lazy loading |
| [@angular/forms](https://angular.io/guide/reactive-forms) | ^17.3.0 | Reactive forms |
| [@angular/material](https://material.angular.io/) | ^17.3.0 | UI components (cards, tables, dialogs, etc.) |
| [@angular/cdk](https://material.angular.io/cdk) | ^17.3.0 | Component Dev Kit (overlay, a11y, etc.) |
| [@angular/animations](https://angular.io/guide/animations) | ^17.3.0 | Animation support |
| [rxjs](https://rxjs.dev/) | ~7.8.0 | Reactive state management (`BehaviorSubject`, operators) |
| [chart.js](https://www.chartjs.org/) | ^4.4.0 | Analytics chart rendering |
| [ng2-charts](https://www.npmjs.com/package/ng2-charts) | ^5.0.0 | Angular wrapper for Chart.js |
| [qrcode](https://www.npmjs.com/package/qrcode) | ^1.5.3 | QR code generation for certificates |
| [tailwindcss](https://tailwindcss.com/) | ^3.4.1 | Utility-first CSS framework |
| [typescript](https://www.typescriptlang.org/) | ~5.3.3 | TypeScript compiler |
| [json-server](https://github.com/typicode/json-server) | ^0.17.4 | Mock REST API for frontend development |

---

## Notable Implementation Techniques

### Backend

- **JWT Authentication**: Stateless, HMAC-SHA256 signed tokens with configurable expiration. Custom `JwtAuthenticationFilter` extends `OncePerRequestFilter`.
- **Spring Security with Method Security**: `@EnableMethodSecurity` + `@PreAuthorize("hasRole('ADMIN')")` on individual controller methods.
- **BCrypt Password Encoding**: `BCryptPasswordEncoder` with strength 12.
- **JPA/Hibernate Optimizations**: Batch inserts (`hibernate.jdbc.batch_size = 30`), ordered inserts/updates, `default_batch_fetch_size = 20`, `open-in-view = false`.
- **MapStruct**: Compile-time entity-to-DTO mapping (`CertificateMapper`, `CourseMapper`, `EmployeeMapper`, `QCMQuestionMapper`).
- **Flyway Migrations**: Versioned SQL migrations with `out-of-order` support.
- **Global Exception Handler**: `@ControllerAdvice` with structured error responses.
- **LLM Question Generation**: `LLMService` calls Google Gemini REST API with PDF-extracted context, parses structured JSON response, creates QCM questions with fallback placeholders.
- **Soft Deletes**: `deleted` boolean column on `Employee`, `Course`, `Department` entities. All queries filter `deleted = false`.
- **Dev Data Initializer**: `CommandLineRunner` with `@Profile("dev")` seeds departments, employees, courses, sections, QCM questions, and sample progress.
- **Base64 Environment Variable JWT Secret**: `JwtTokenProvider` decodes Base64-encoded secret for `hmacShaKeyFor`.
- **H2 PostgreSQL Compatibility Mode**: Dev profile uses `MODE=PostgreSQL` for near-production SQL dialect compatibility.

### Frontend

- **Functional Route Guards**: `authGuard` and `roleGuard` are `CanActivateFn` functions (not classes).
- **Functional HTTP Interceptor**: `jwtInterceptor` is an `HttpInterceptorFn` (not a class).
- **Optimistic UI Updates**: `NotificationService` marks notifications as read locally before server confirmation; rolls back on error.
- **RxJS BehaviorSubject State**: `AuthService.currentUser$` and `NotificationService.notifications$` / `unreadCount$` for reactive UI updates.
- **Standalone Components**: No `NgModule` declarations. Each component is self-declaring with `standalone: true`.
- **Tailwind CSS Design Token System**: CSS custom properties for colors, shadows, spacing. Dark mode via `.dark` class.
- **Angular Material Custom Theming**: SCSS overrides for cards, form fields, tables, dialogs, snackbars, tooltips to match brand style.
- **Lazy-Loaded Feature Routes**: Admin and employee sections are lazy-loaded via `loadChildren`.
- **Environment-Specific Configuration**: `environment.ts` vs `environment.prod.ts` for API URLs and Cloudinary settings.
- **Cloudinary File Upload**: Support for standard and chunked uploads with progress tracking.

---

## Project Structure

```
PoodleLMS/
├── backend_springboot/
│   ├── .env                          # Local environment variables (gitignored)
│   ├── .env.example                  # Environment variable template
│   ├── Dockerfile                    # Container build
│   ├── pom.xml                       # Maven build with dependencies
│   └── src/
│       ├── main/
│       │   ├── java/com/enterprise/poodle/
│       │   │   ├── PoodleApplication.java        # Spring Boot entry point
│       │   │   ├── config/
│       │   │   │   ├── DevDataInitializer.java   # Dev profile seed data
│       │   │   │   ├── DotEnvPostProcessor.java  # (empty)
│       │   │   │   ├── JacksonConfig.java        # ObjectMapper config
│       │   │   │   ├── LLMConfig.java            # Gemini API RestTemplate
│       │   │   │   ├── OpenApiConfig.java        # Swagger/OpenAPI config
│       │   │   │   └── SecurityConfig.java       # Spring Security + CORS
│       │   │   ├── controller/       # 12 REST controllers
│       │   │   ├── dto/
│       │   │   │   ├── request/      # 12 request DTOs
│       │   │   │   └── response/     # 20 response DTOs
│       │   │   ├── entity/           # 14 JPA entities
│       │   │   ├── enums/            # 7 enums (Role, CourseStatus, etc.)
│       │   │   ├── exception/        # 5 exception classes + handler
│       │   │   ├── mapper/           # 4 MapStruct mappers
│       │   │   ├── repository/       # 15 Spring Data JPA repositories
│       │   │   ├── security/
│       │   │   │   ├── CustomUserDetailsService.java
│       │   │   │   ├── JwtAuthenticationFilter.java
│       │   │   │   ├── JwtTokenProvider.java
│       │   │   │   └── SecurityUtils.java
│       │   │   └── service/          # 14 service classes
│       │   └── resources/
│       │       ├── application.yml              # Main config (PostgreSQL)
│       │       ├── application-dev.yml          # Dev config (H2)
│       │       ├── data-dev.sql                 # Dev SQL seed
│       │       └── db/migration/                # Flyway migrations
│       │           ├── V1__init_schema.sql
│       │           ├── V2__seed_test_data.sql
│       │           ├── V3__add_attempt_answers_table.sql
│       │           ├── V4__add_image_content_type.sql
│       │           └── V5__reset_sequences_h2.sql
│       └── test/
│           ├── java/com/enterprise/poodle/
│           │   ├── BaseIntegrationTest.java
│           │   ├── controller/       # 9 controller integration tests
│           │   ├── security/         # 1 security diagnostic test
│           │   └── service/          # 4 service unit tests
│           └── resources/
│               └── application-test.yml
│
└── frontend_angular/
    ├── angular.json                  # Angular CLI config (standalone components)
    ├── package.json                  # npm dependencies
    ├── tailwind.config.js            # Tailwind CSS theme
    ├── tsconfig.json / tsconfig.app.json / tsconfig.spec.json
    └── src/
        ├── index.html                # Entry HTML (Montserrat font, Material Icons)
        ├── main.ts                   # Bootstrap standalone app
        ├── styles.scss               # Global styles, Tailwind + Material overrides
        ├── _redirects                # Static hosting redirect rules
        ├── assets/                   # Static assets
        ├── environments/
        │   ├── environment.ts        # Dev: apiUrl = localhost:8081
        │   └── environment.prod.ts   # Prod: apiUrl = render.com
        └── app/
            ├── app.component.ts / .html / .spec.ts
            ├── app.config.ts         # App providers (router, HTTP, animations)
            ├── app.routes.ts         # Root routes (login, verify, admin, employee)
            ├── auth/
            │   └── login/            # LoginComponent
            ├── admin/
            │   ├── admin.routes.ts   # Lazy-loaded admin routes
            │   ├── admin-layout/     # AdminLayoutComponent (sidebar + navbar)
            │   ├── dashboard/        # AdminDashboardComponent
            │   ├── analytics/        # AdminAnalyticsComponent (Chart.js)
            │   ├── courses/
            │   │   ├── course-list/  # AdminCoursesComponent
            │   │   └── course-detail/ # AdminCourseDetailComponent
            │   ├── departments/      # AdminDepartmentsComponent
            │   ├── employees/
            │   │   ├── admin-employees.component
            │   │   └── employee-progress/
            │   └── certificates/     # AdminCertificatesComponent
            ├── employee/
            │   ├── employee.routes.ts # Lazy-loaded employee routes
            │   ├── employee-layout/  # EmployeeLayoutComponent
            │   ├── dashboard/        # EmployeeDashboardComponent
            │   ├── courses/
            │   │   ├── course-list/  # EmployeeCoursesComponent
            │   │   └── course-detail/ # EmployeeCourseDetailComponent
            │   └── certificates/    # EmployeeCertificatesComponent
            ├── core/
            │   ├── guards/
            │   │   ├── auth.guard.ts # Auth guard (redirects to /login)
            │   │   ├── role.guard.ts # Role guard (ADMIN/EMPLOYEE)
            │   │   └── index.ts
            │   ├── interceptors/
            │   │   ├── jwt.interceptor.ts  # JWT attachment + 401 handling
            │   │   └── index.ts
            │   ├── services/
            │   │   ├── auth.service.ts
            │   │   ├── api.service.ts
            │   │   ├── course.service.ts
            │   │   ├── employee.service.ts
            │   │   ├── department.service.ts
            │   │   ├── enrollment.service.ts
            │   │   ├── certificate.service.ts
            │   │   ├── notification.service.ts
            │   │   ├── analytics.service.ts
            │   │   ├── cloudinary-upload.service.ts
            │   │   ├── cloudinary-asset.service.ts
            │   │   ├── chunked-upload.service.ts
            │   │   ├── snackbar.service.ts
            │   │   ├── toast.service.ts
            │   │   ├── logo.service.ts
            │   │   ├── me.service.ts (empty)
            │   │   └── index.ts
            │   └── utils/
            │       └── modal-config.ts
            ├── models/
            │   ├── index.ts
            │   ├── user.model.ts
            │   ├── course.model.ts
            │   ├── department.model.ts
            │   ├── enrollment.model.ts
            │   ├── certificate.model.ts
            │   └── analytics.model.ts
            └── shared/
                └── components/
                    ├── index.ts
                    ├── status-badge/
                    ├── skeleton/
                    ├── empty-state/
                    ├── loading-spinner/
                    ├── page-header/
                    ├── confirm-dialog/
                    ├── form-dialog/
                    ├── notification-bell/
                    ├── notification-modal/
                    ├── qr-code/
                    └── certificate-verify/
```

### Directory Descriptions

**`backend_springboot/src/main/java/com/enterprise/poodle/`** — Standard Spring Boot layered architecture:
- `config/` — Application configuration (security, OpenAPI, Jackson, LLM, dev data).
- `controller/` — REST endpoints, one per domain aggregate.
- `dto/request/` — Input validation models.
- `dto/response/` — Output serialization models (immutable builders).
- `entity/` — JPA entities with relationships, indexes, and soft-delete support.
- `enums/` — Type-safe enumerations for domain concepts.
- `exception/` — Custom exceptions + global handler.
- `mapper/` — MapStruct interfaces for entity-to-DTO mapping.
- `repository/` — Spring Data JPA interfaces with custom queries.
- `security/` — JWT authentication infrastructure.
- `service/` — Business logic layer with `@Transactional` support.

**`backend_springboot/src/main/resources/db/migration/`** — Flyway versioned migration scripts maintaining the database schema.

**`backend_springboot/src/test/`** — Integration tests for controllers (with nested test classes) and service unit tests.

**`frontend_angular/src/app/`** — Angular application with feature-based directory organization:
- `admin/` — Admin-only features (dashboard, courses, departments, employees, certificates, analytics).
- `employee/` — Employee features (dashboard, courses, certificates).
- `auth/` — Authentication (login).
- `core/` — Singleton services, guards, interceptors.
- `models/` — TypeScript interfaces matching backend DTOs.
- `shared/` — Reusable UI components used across admin and employee modules.

---

## Development Notes

### Prerequisites

- Java 21+
- Node.js 18+
- Docker (optional, for containerized backend)

### Backend (Spring Boot)

```bash
cd backend_springboot
# Set up environment
cp .env.example .env
# Edit .env with your values

# Run with dev profile (H2 in-memory)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
./mvnw test

# Build
./mvnw clean package -DskipTests
```

Default dev credentials (seeded by `DevDataInitializer`):
- Admin: `admin@poodle.com` / `Admin@123`
- Employee: `john.doe@poodle.com` / `Employee@123`

### Frontend (Angular)

```bash
cd frontend_angular
npm install
ng serve                    # Development server on port 4200
npm run start:mock          # With JSON server mock backend
```

### Database Migrations

Migrations are applied automatically by Flyway on application startup. To create a new migration, add a `V{next}__description.sql` file to `src/main/resources/db/migration/`.

### Docker

```bash
cd backend_springboot
docker build -t poodle-lms-backend .
docker run -p 8081:8081 --env-file .env poodle-lms-backend
```

### API Documentation

When the backend is running, Swagger UI is available at:
- `http://localhost:8081/swagger-ui.html`
- OpenAPI spec: `http://localhost:8081/v3/api-docs`

### LLM QCM Generation

To enable AI-powered QCM question generation:
1. Obtain a [Google Gemini API key](https://aistudio.google.com/).
2. Set `GEMINI_API_KEY=<your-key>` in `.env` or environment variable.
3. Optionally override `GEMINI_MODEL` (default: `gemini-2.5-flash`).
4. On a QCM section, the "Generate QCM Draft" action will use Gemini to create questions from the section's PDF content.

### Notes

- The backend uses soft deletes (`deleted` boolean) for `Course`, `Employee`, and `Department` entities.
- JWT tokens expire after 24 hours by default (configurable via `JWT_EXPIRATION_MS`).
- CORS allows requests from `http://localhost:3000` and `http://localhost:4200` by default.
- The frontend mock server (`npm run start:mock`) uses `json-server` for standalone frontend development without a running backend.
