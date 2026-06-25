package com.enterprise.poodle.controller;

import com.enterprise.poodle.BaseIntegrationTest;
import com.enterprise.poodle.dto.request.EmployeeUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Employee Controller Tests")
class EmployeeControllerTest extends BaseIntegrationTest {

    @Nested
    @DisplayName("GET /api/employees")
    class GetAllEmployeesTests {

        @Test
        @DisplayName("Should return paginated employees for admin")
        void getAllEmployeesAsAdmin() throws Exception {
            mockMvc.perform(get("/api/employees")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(3)))
                    .andExpect(jsonPath("$.totalElements").isNumber());
        }

        @Test
        @DisplayName("Should support pagination parameters")
        void getAllEmployeesPaginated() throws Exception {
            mockMvc.perform(get("/api/employees")
                            .param("page", "0")
                            .param("size", "1")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.totalPages").value(greaterThanOrEqualTo(3)));
        }

        @Test
        @DisplayName("Should return 403 for employee role")
        void getAllEmployeesForbidden() throws Exception {
            mockMvc.perform(get("/api/employees")
                            .header("Authorization", "Bearer " + employeeToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 without token")
        void getAllEmployeesUnauthorized() throws Exception {
            mockMvc.perform(get("/api/employees"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/employees/{id}")
    class GetEmployeeByIdTests {

        @Test
        @DisplayName("Should return employee by ID")
        void getEmployeeById() throws Exception {
            mockMvc.perform(get("/api/employees/{id}", employeeUser.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(employeeUser.getId()))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.email").value("employee@test.com"))
                    .andExpect(jsonPath("$.role").value("ROLE_EMPLOYEE"))
                    .andExpect(jsonPath("$.departmentId").value(backend.getId()));
        }

        @Test
        @DisplayName("Should return 404 for non-existent employee")
        void getEmployeeNotFound() throws Exception {
            mockMvc.perform(get("/api/employees/{id}", 99999)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/employees/{id}")
    class UpdateEmployeeTests {

        @Test
        @DisplayName("Should update employee name and department")
        void updateEmployee() throws Exception {
            EmployeeUpdateRequest request = new EmployeeUpdateRequest();
            request.setFirstName("Updated");
            request.setLastName("Name");
            request.setDepartmentId(frontend.getId());

            mockMvc.perform(put("/api/employees/{id}", employeeUser.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Updated"))
                    .andExpect(jsonPath("$.lastName").value("Name"))
                    .andExpect(jsonPath("$.departmentId").value(frontend.getId()));
        }

        @Test
        @DisplayName("Should update employee email")
        void updateEmployeeEmail() throws Exception {
            EmployeeUpdateRequest request = new EmployeeUpdateRequest();
            request.setEmail("newemail@test.com");

            mockMvc.perform(put("/api/employees/{id}", employeeUser.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("newemail@test.com"));
        }

        @Test
        @DisplayName("Should return 409 for duplicate email")
        void updateEmployeeDuplicateEmail() throws Exception {
            EmployeeUpdateRequest request = new EmployeeUpdateRequest();
            request.setEmail("admin@test.com"); // already taken

            mockMvc.perform(put("/api/employees/{id}", employeeUser.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 404 for non-existent employee")
        void updateNonExistentEmployee() throws Exception {
            EmployeeUpdateRequest request = new EmployeeUpdateRequest();
            request.setFirstName("Ghost");

            mockMvc.perform(put("/api/employees/{id}", 99999)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/employees/{id}")
    class DeleteEmployeeTests {

        @Test
        @DisplayName("Should soft-delete employee")
        void deleteEmployee() throws Exception {
            mockMvc.perform(delete("/api/employees/{id}", employeeUser2.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNoContent());

            // Verify it's no longer found
            mockMvc.perform(get("/api/employees/{id}", employeeUser2.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 for non-existent employee")
        void deleteNonExistentEmployee() throws Exception {
            mockMvc.perform(delete("/api/employees/{id}", 99999)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }
}
