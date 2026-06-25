package com.enterprise.poodle.service;

import com.enterprise.poodle.dto.response.CertificateResponse;
import com.enterprise.poodle.dto.response.CertificateVerificationResponse;
import com.enterprise.poodle.entity.Certificate;
import com.enterprise.poodle.entity.Course;
import com.enterprise.poodle.entity.Employee;
import com.enterprise.poodle.exception.BusinessException;
import com.enterprise.poodle.exception.ResourceNotFoundException;
import com.enterprise.poodle.mapper.CertificateMapper;
import com.enterprise.poodle.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final CertificateMapper certificateMapper;

    @Transactional
    public void generateCertificateIfEligible(Employee employee, Course course) {
        // Check if certificate already exists
        if (certificateRepository.existsByEmployeeIdAndCourseId(
                employee.getId(), course.getId())) {
            log.info("Certificate already exists for employee {} and course {}",
                    employee.getId(), course.getId());
            return;
        }

        Certificate certificate = Certificate.builder()
                .employee(employee)
                .course(course)
                .issuedAt(LocalDateTime.now())
                .build();

        
        int attempts = 0;
        final int maxAttempts = 3;
        while (true) {
            try {
                
                String qrUrl = String.format("/api/certificates/verify/%s",
                        certificate.getCertificateCode());
                certificate.setQrCodeUrl(qrUrl);

                // save and flush to detect unique constraint violations immediately
                certificateRepository.saveAndFlush(certificate);
                log.info("Certificate generated for employee {} - course {}: {}",
                        employee.getId(), course.getId(), certificate.getCertificateCode());
                break;
            } catch (DataIntegrityViolationException ex) {
                attempts++;
                log.warn("Failed to save certificate due to data integrity violation (attempt {}): {}", attempts, ex.getMessage());
                if (attempts >= maxAttempts) {
                    throw ex; // rethrow after retries exhausted
                }
                // regenerate a new code and retry
                certificate.setCertificateCode(UUID.randomUUID().toString());
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<CertificateResponse> getMyCertificates(Long employeeId, Pageable pageable) {
        return certificateRepository.findByEmployeeId(employeeId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<CertificateResponse> getAllCertificates(Pageable pageable) {
        return certificateRepository.findAllWithEagerLoading(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public CertificateVerificationResponse verifyCertificate(String certificateCode) {
        Certificate certificate = certificateRepository.findByCertificateCode(certificateCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Certificate", "code", certificateCode));

        Employee employee = certificate.getEmployee();
        Course course = certificate.getCourse();

        return CertificateVerificationResponse.builder()
                .employeeName(employee.getFirstName() + " " + employee.getLastName())
                .courseTitle(course.getTitle())
                .issuedAt(certificate.getIssuedAt())
                .status(certificate.isRevoked() ? "REVOKED" : "VALID")
                .build();
    }

    @Transactional
    public CertificateResponse revokeCertificate(Long id) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate", "id", id));

        if (certificate.isRevoked()) {
            throw new BusinessException("Certificate is already revoked");
        }

        certificate.setRevoked(true);
        certificateRepository.save(certificate);
        log.info("Certificate {} revoked", certificate.getCertificateCode());
        return mapToResponse(certificate);
    }

    @Transactional
    public CertificateResponse unrevokeCertificate(Long id) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate", "id", id));

        if (!certificate.isRevoked()) {
            throw new BusinessException("Certificate is not revoked");
        }

        certificate.setRevoked(false);
        certificateRepository.save(certificate);
        log.info("Certificate {} unrevoked", certificate.getCertificateCode());
        return mapToResponse(certificate);
    }

    private CertificateResponse mapToResponse(Certificate certificate) {
        return certificateMapper.toResponse(certificate);
    }
}
