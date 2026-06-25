package com.enterprise.poodle.controller;

import com.enterprise.poodle.BaseIntegrationTest;
import com.enterprise.poodle.dto.request.DepartmentRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Department Controller Tests")
class DepartmentControllerTest extends BaseIntegrationTest {

    @Nested
    @DisplayName("POST /api/departments")
    class CreateDepartmentTests {

        @Test
        @DisplayName("Should create a root department")
        void createRootDepartment() throws Exception {
            DepartmentRequest request = new DepartmentRequest();
            request.setName("Finance");

            mockMvc.perform(post("/api/departments")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.name").value("Finance"))
                    .andExpect(jsonPath("$.parentDepartmentId").isEmpty());
        }

        @Test
        @DisplayName("Should create a child department")
        void createChildDepartment() throws Exception {
            DepartmentRequest request = new DepartmentRequest();
            request.setName("DevOps");
            request.setParentDepartmentId(engineering.getId());

            mockMvc.perform(post("/api/departments")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("DevOps"))
                    .andExpect(jsonPath("$.parentDepartmentId").value(engineering.getId()))
                    .andExpect(jsonPath("$.parentDepartmentName").value("Engineering"));
        }

        @Test
        @DisplayName("Should return 400 for missing name")
        void createDepartmentMissingName() throws Exception {
            DepartmentRequest request = new DepartmentRequest();

            mockMvc.perform(post("/api/departments")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 for non-existent parent")
        void createDepartmentInvalidParent() throws Exception {
            DepartmentRequest request = new DepartmentRequest();
            request.setName("Orphan");
            request.setParentDepartmentId(99999L);

            mockMvc.perform(post("/api/departments")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 for employee role")
        void createDepartmentForbidden() throws Exception {
            DepartmentRequest request = new DepartmentRequest();
            request.setName("Nope");

            mockMvc.perform(post("/api/departments")
                            .header("Authorization", "Bearer " + employeeToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/departments")
    class GetAllDepartmentsTests {

        @Test
        @DisplayName("Should return all departments")
        void getAllDepartments() throws Exception {
            mockMvc.perform(get("/api/departments")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(4)); // engineering, backend, frontend, hr
        }
    }

    @Nested
    @DisplayName("GET /api/departments/tree")
    class GetDepartmentTreeTests {

        @Test
        @DisplayName("Should return department tree hierarchy")
        void getDepartmentTree() throws Exception {
            mockMvc.perform(get("/api/departments/tree")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2)) // Engineering, HR (roots)
                    .andExpect(jsonPath("$[?(@.name=='Engineering')].children.length()").value(2)); // Backend, Frontend
        }
    }

    @Nested
    @DisplayName("PUT /api/departments/{id}")
    class UpdateDepartmentTests {

        @Test
        @DisplayName("Should update department name")
        void updateDepartment() throws Exception {
            DepartmentRequest request = new DepartmentRequest();
            request.setName("Backend Engineering (Updated)");

            mockMvc.perform(put("/api/departments/{id}", backend.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Backend Engineering (Updated)"));
        }

        @Test
        @DisplayName("Should return 400 when setting self as parent")
        void updateDepartmentSelfParent() throws Exception {
            DepartmentRequest request = new DepartmentRequest();
            request.setName("Engineering");
            request.setParentDepartmentId(engineering.getId());

            mockMvc.perform(put("/api/departments/{id}", engineering.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 for non-existent department")
        void updateNonExistentDepartment() throws Exception {
            DepartmentRequest request = new DepartmentRequest();
            request.setName("Ghost");

            mockMvc.perform(put("/api/departments/{id}", 99999)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/departments/{id}")
    class DeleteDepartmentTests {

        @Test
        @DisplayName("Should soft-delete department and children")
        void deleteDepartment() throws Exception {
            mockMvc.perform(delete("/api/departments/{id}", engineering.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 404 for non-existent department")
        void deleteNonExistentDepartment() throws Exception {
            mockMvc.perform(delete("/api/departments/{id}", 99999)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }
}
