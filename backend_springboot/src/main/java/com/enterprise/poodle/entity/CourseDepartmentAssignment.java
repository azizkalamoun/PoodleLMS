package com.enterprise.poodle.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "course_department_assignments", indexes = {
        @Index(name = "idx_cda_course", columnList = "course_id"),
        @Index(name = "idx_cda_department", columnList = "department_id"),
        @Index(name = "idx_cda_deadline", columnList = "deadline_date")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_course_department", columnNames = {"course_id", "department_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDepartmentAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "deadline_date")
    private LocalDate deadlineDate;
}
