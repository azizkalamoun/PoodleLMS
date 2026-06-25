package com.enterprise.poodle.controller;

import com.enterprise.poodle.dto.response.CertificateResponse;
import com.enterprise.poodle.dto.response.CertificateVerificationResponse;
import com.enterprise.poodle.security.SecurityUtils;
import com.enterprise.poodle.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;
    private final SecurityUtils securityUtils;

    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Page<CertificateResponse>> getMyCertificates(Pageable pageable) {
        Long employeeId = securityUtils.getCurrentEmployeeId();
        return ResponseEntity.ok(certificateService.getMyCertificates(employeeId, pageable));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CertificateResponse>> getAllCertificates(Pageable pageable) {
        return ResponseEntity.ok(certificateService.getAllCertificates(pageable));
    }

    @GetMapping("/verify/{certificateCode}")
    public ResponseEntity<CertificateVerificationResponse> verifyCertificate(
            @PathVariable String certificateCode) {
        return ResponseEntity.ok(certificateService.verifyCertificate(certificateCode));
    }

    @PutMapping("/{id}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CertificateResponse> revokeCertificate(@PathVariable Long id) {
        return ResponseEntity.ok(certificateService.revokeCertificate(id));
    }

    @PutMapping("/{id}/unrevoke")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CertificateResponse> unrevokeCertificate(@PathVariable Long id) {
        return ResponseEntity.ok(certificateService.unrevokeCertificate(id));
    }
}
