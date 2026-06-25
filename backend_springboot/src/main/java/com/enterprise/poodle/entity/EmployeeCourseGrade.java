package com.enterprise.poodle.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_course_grades", indexes = {
        @Index(name = "idx_grade_employee", columnList = "employee_id"),
        @Index(name = "idx_grade_course", columnList = "course_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_employee_course_grade", columnNames = {"employee_id", "course_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeCourseGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "final_score", nullable = false)
    private Integer finalScore;

    @Column(nullable = false)
    private boolean passed;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
