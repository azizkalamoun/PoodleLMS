package com.enterprise.poodle.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CertificateVerificationResponse {
    private String employeeName;
    private String courseTitle;
    private LocalDateTime issuedAt;
    private String status; // "VALID" or "REVOKED"
}
