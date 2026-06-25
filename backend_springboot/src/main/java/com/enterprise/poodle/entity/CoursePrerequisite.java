package com.enterprise.poodle.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_prerequisites", indexes = {
        @Index(name = "idx_prereq_course", columnList = "course_id"),
        @Index(name = "idx_prereq_prerequisite", columnList = "prerequisite_course_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_course_prerequisite", columnNames = {"course_id", "prerequisite_course_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoursePrerequisite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prerequisite_course_id", nullable = false)
    private Course prerequisiteCourse;
}
