package com.enterprise.poodle.controller;

import com.enterprise.poodle.BaseIntegrationTest;
import com.enterprise.poodle.entity.Certificate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Certificate Controller Tests")
class CertificateControllerTest extends BaseIntegrationTest {

    @Nested
    @DisplayName("GET /api/certificates/my")
    class GetMyCertificatesTests {

        @Test
        @DisplayName("Should return employee's certificates")
        void getMyCertificates() throws Exception {
            // Create a certificate for the employee
            String certCode = UUID.randomUUID().toString();
            certificateRepository.save(Certificate.builder()
                    .employee(employeeUser).course(publishedCourse)
                    .certificateCode(certCode)
                    .qrCodeUrl("/api/certificates/verify/" + certCode)
                    .issuedAt(LocalDateTime.now()).build());

            mockMvc.perform(get("/api/certificates/my")
                            .header("Authorization", "Bearer " + employeeToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].courseTitle").value("Test Course"))
                    .andExpect(jsonPath("$.content[0].certificateCode").value(certCode))
                    .andExpect(jsonPath("$.content[0].revoked").value(false));
        }

        @Test
        @DisplayName("Should return empty list when no certificates")
        void getMyCertificatesEmpty() throws Exception {
            mockMvc.perform(get("/api/certificates/my")
                            .header("Authorization", "Bearer " + employeeToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(0));
        }

        @Test
        @DisplayName("Should return 403 for admin")
        void getMyCertificatesForbiddenForAdmin() throws Exception {
            mockMvc.perform(get("/api/certificates/my")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/certificates/verify/{certificateCode}")
    class VerifyCertificateTests {

        @Test
        @DisplayName("Should verify a valid certificate (public endpoint)")
        void verifyCertificate() throws Exception {
            String certCode = UUID.randomUUID().toString();
            certificateRepository.save(Certificate.builder()
                    .employee(employeeUser).course(publishedCourse)
                    .certificateCode(certCode)
                    .qrCodeUrl("/api/certificates/verify/" + certCode)
                    .issuedAt(LocalDateTime.now()).build());

            // Public endpoint - no token needed
            mockMvc.perform(get("/api/certificates/verify/{code}", certCode))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.employeeName").value("John Doe"))
                    .andExpect(jsonPath("$.courseTitle").value("Test Course"))
                    .andExpect(jsonPath("$.status").value("VALID"));
        }

        @Test
        @DisplayName("Should verify a revoked certificate")
        void verifyRevokedCertificate() throws Exception {
            String certCode = UUID.randomUUID().toString();
            certificateRepository.save(Certificate.builder()
                    .employee(employeeUser).course(publishedCourse)
                    .certificateCode(certCode)
                    .qrCodeUrl("/api/certificates/verify/" + certCode)
                    .issuedAt(LocalDateTime.now())
                    .revoked(true).build());

            mockMvc.perform(get("/api/certificates/verify/{code}", certCode))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REVOKED"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent certificate")
        void verifyCertificateNotFound() throws Exception {
            mockMvc.perform(get("/api/certificates/verify/{code}", "non-existent-code"))
                    .andExpect(status().isNotFound());
        }
    }
}
