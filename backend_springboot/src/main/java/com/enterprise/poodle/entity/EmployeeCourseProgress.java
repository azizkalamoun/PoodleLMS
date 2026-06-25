package com.enterprise.poodle.entity;

import com.enterprise.poodle.enums.ProgressStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_course_progress", indexes = {
        @Index(name = "idx_progress_employee", columnList = "employee_id"),
        @Index(name = "idx_progress_course", columnList = "course_id"),
        @Index(name = "idx_progress_section", columnList = "section_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_employee_section_progress", columnNames = {"employee_id", "section_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeCourseProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private CourseSection section;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProgressStatus status = ProgressStatus.NOT_STARTED;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
