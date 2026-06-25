package com.enterprise.poodle.service;

import com.enterprise.poodle.dto.response.CertificateResponse;
import com.enterprise.poodle.dto.response.CertificateVerificationResponse;
import com.enterprise.poodle.entity.Certificate;
import com.enterprise.poodle.entity.Course;
import com.enterprise.poodle.entity.Employee;
import com.enterprise.poodle.enums.CourseStatus;
import com.enterprise.poodle.enums.Role;
import com.enterprise.poodle.exception.BusinessException;
import com.enterprise.poodle.exception.ResourceNotFoundException;
import com.enterprise.poodle.mapper.CertificateMapper;
import com.enterprise.poodle.repository.CertificateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificateServiceTest {

    @Mock private CertificateRepository certificateRepository;
    @Mock private CertificateMapper certificateMapper;

    @InjectMocks
    private CertificateService certificateService;

    private Employee employee;
    private Course course;
    private Certificate certificate;
    private CertificateResponse certificateResponse;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@poodle.com")
                .role(Role.ROLE_EMPLOYEE)
                .build();

        course = Course.builder()
                .id(1L)
                .title("Spring Boot")
                .status(CourseStatus.PUBLISHED)
                .passingScore(70)
                .build();

        certificate = Certificate.builder()
                .id(1L)
                .employee(employee)
                .course(course)
                .certificateCode("CERT-12345")
                .qrCodeUrl("/api/certificates/verify/CERT-12345")
                .issuedAt(LocalDateTime.now())
                .revoked(false)
                .build();

        certificateResponse = CertificateResponse.builder()
                .id(1L)
                .employeeName("John Doe")
                .courseTitle("Spring Boot")
                .certificateCode("CERT-12345")
                .issuedAt(certificate.getIssuedAt())
                .revoked(false)
                .build();
    }

    @Nested
    @DisplayName("generateCertificateIfEligible")
    class GenerateCertificate {
        @Test
        void shouldGenerateCertificate_whenNoneExists() {
            when(certificateRepository.existsByEmployeeIdAndCourseId(1L, 1L)).thenReturn(false);
            when(certificateRepository.saveAndFlush(any(Certificate.class))).thenReturn(certificate);

            certificateService.generateCertificateIfEligible(employee, course);

            verify(certificateRepository).saveAndFlush(any(Certificate.class));
        }

        @Test
        void shouldSkip_whenCertificateAlreadyExists() {
            when(certificateRepository.existsByEmployeeIdAndCourseId(1L, 1L)).thenReturn(true);

            certificateService.generateCertificateIfEligible(employee, course);

            verify(certificateRepository, never()).save(any(Certificate.class));
        }
    }

    @Nested
    @DisplayName("getMyCertificates")
    class GetMyCertificates {
        @Test
        void shouldReturnPaginatedCertificates() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Certificate> page = new PageImpl<>(List.of(certificate), pageable, 1);

            when(certificateRepository.findByEmployeeId(1L, pageable)).thenReturn(page);
            when(certificateMapper.toResponse(certificate)).thenReturn(certificateResponse);

            Page<CertificateResponse> result = certificateService.getMyCertificates(1L, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getCertificateCode()).isEqualTo("CERT-12345");
        }
    }

    @Nested
    @DisplayName("verifyCertificate")
    class VerifyCertificate {
        @Test
        void shouldReturnValid_whenCertificateIsNotRevoked() {
            when(certificateRepository.findByCertificateCode("CERT-12345"))
                    .thenReturn(Optional.of(certificate));

            CertificateVerificationResponse result =
                    certificateService.verifyCertificate("CERT-12345");

            assertThat(result.getStatus()).isEqualTo("VALID");
            assertThat(result.getEmployeeName()).isEqualTo("John Doe");
            assertThat(result.getCourseTitle()).isEqualTo("Spring Boot");
        }

        @Test
        void shouldReturnRevoked_whenCertificateIsRevoked() {
            certificate.setRevoked(true);
            when(certificateRepository.findByCertificateCode("CERT-12345"))
                    .thenReturn(Optional.of(certificate));

            CertificateVerificationResponse result =
                    certificateService.verifyCertificate("CERT-12345");

            assertThat(result.getStatus()).isEqualTo("REVOKED");
        }

        @Test
        void shouldThrowNotFound_whenCertificateCodeInvalid() {
            when(certificateRepository.findByCertificateCode("INVALID"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> certificateService.verifyCertificate("INVALID"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("revokeCertificate")
    class RevokeCertificate {
        @Test
        void shouldRevokeCertificate() {
            when(certificateRepository.findById(1L)).thenReturn(Optional.of(certificate));
            when(certificateRepository.save(any(Certificate.class))).thenReturn(certificate);
            when(certificateMapper.toResponse(any(Certificate.class))).thenReturn(certificateResponse);

            CertificateResponse result = certificateService.revokeCertificate(1L);

            assertThat(certificate.isRevoked()).isTrue();
            verify(certificateRepository).save(certificate);
        }

        @Test
        void shouldThrowBusinessException_whenAlreadyRevoked() {
            certificate.setRevoked(true);
            when(certificateRepository.findById(1L)).thenReturn(Optional.of(certificate));

            assertThatThrownBy(() -> certificateService.revokeCertificate(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Certificate is already revoked");
        }

        @Test
        void shouldThrowNotFound_whenCertificateDoesNotExist() {
            when(certificateRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> certificateService.revokeCertificate(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
