package com.enterprise.poodle.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "certificates", indexes = {
        @Index(name = "idx_cert_employee", columnList = "employee_id"),
        @Index(name = "idx_cert_course", columnList = "course_id"),
        @Index(name = "idx_cert_code", columnList = "certificate_code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "certificate_code", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private String certificateCode = UUID.randomUUID().toString();

    @Column(name = "qr_code_url")
    private String qrCodeUrl;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;
}
