package com.enterprise.poodle.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CertificateResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long courseId;
    private String courseTitle;
    private Integer score;
    private String certificateCode;
    private String verificationCode;
    private String qrCodeUrl;
    private LocalDateTime issuedAt;
    private boolean revoked;
}
