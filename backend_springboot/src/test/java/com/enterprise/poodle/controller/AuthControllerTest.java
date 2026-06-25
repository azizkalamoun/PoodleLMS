package com.enterprise.poodle.controller;

import com.enterprise.poodle.BaseIntegrationTest;
import com.enterprise.poodle.dto.request.LoginRequest;
import com.enterprise.poodle.dto.request.RegisterRequest;
import com.enterprise.poodle.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Auth Controller Tests")
class AuthControllerTest extends BaseIntegrationTest {

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void loginSuccess() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail("admin@test.com");
            request.setPassword("Admin@123");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.email").value("admin@test.com"))
                    .andExpect(jsonPath("$.role").value("ROLE_ADMIN"))
                    .andExpect(jsonPath("$.firstName").value("Admin"))
                    .andExpect(jsonPath("$.lastName").value("User"));
        }

        @Test
        @DisplayName("Should return 401 for invalid password")
        void loginInvalidPassword() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail("admin@test.com");
            request.setPassword("WrongPassword");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 401 for non-existent email")
        void loginNonExistentEmail() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail("nonexistent@test.com");
            request.setPassword("Password@123");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 for missing email")
        void loginMissingEmail() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setPassword("Admin@123");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for invalid email format")
        void loginInvalidEmailFormat() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail("not-an-email");
            request.setPassword("Admin@123");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterTests {

        @Test
        @DisplayName("Should register a new employee as admin")
        void registerSuccess() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setFirstName("New");
            request.setLastName("Employee");
            request.setEmail("new.employee@test.com");
            request.setPassword("NewPass@123");
            request.setRole(Role.ROLE_EMPLOYEE);
            request.setDepartmentId(backend.getId());

            mockMvc.perform(post("/api/auth/register")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.firstName").value("New"))
                    .andExpect(jsonPath("$.lastName").value("Employee"))
                    .andExpect(jsonPath("$.email").value("new.employee@test.com"))
                    .andExpect(jsonPath("$.role").value("ROLE_EMPLOYEE"))
                    .andExpect(jsonPath("$.departmentId").value(backend.getId()));
        }

        @Test
        @DisplayName("Should register admin without department")
        void registerAdminNoDepartment() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setFirstName("New");
            request.setLastName("Admin");
            request.setEmail("new.admin@test.com");
            request.setPassword("NewPass@123");
            request.setRole(Role.ROLE_ADMIN);

            mockMvc.perform(post("/api/auth/register")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.role").value("ROLE_ADMIN"))
                    .andExpect(jsonPath("$.departmentId").isEmpty());
        }

        @Test
        @DisplayName("Should return 403 when employee tries to register")
        void registerForbiddenForEmployee() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setFirstName("No");
            request.setLastName("Access");
            request.setEmail("noaccess@test.com");
            request.setPassword("NewPass@123");
            request.setRole(Role.ROLE_EMPLOYEE);

            mockMvc.perform(post("/api/auth/register")
                            .header("Authorization", "Bearer " + employeeToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 without token")
        void registerUnauthorized() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setFirstName("No");
            request.setLastName("Token");
            request.setEmail("notoken@test.com");
            request.setPassword("NewPass@123");
            request.setRole(Role.ROLE_EMPLOYEE);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 409 for duplicate email")
        void registerDuplicateEmail() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setFirstName("Dup");
            request.setLastName("User");
            request.setEmail("admin@test.com"); // already exists
            request.setPassword("NewPass@123");
            request.setRole(Role.ROLE_EMPLOYEE);

            mockMvc.perform(post("/api/auth/register")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 400 for missing required fields")
        void registerMissingFields() throws Exception {
            RegisterRequest request = new RegisterRequest();
            // all empty

            mockMvc.perform(post("/api/auth/register")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
