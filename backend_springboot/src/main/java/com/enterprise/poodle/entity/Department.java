package com.enterprise.poodle.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments", indexes = {
        @Index(name = "idx_department_parent", columnList = "parent_department_id"),
        @Index(name = "idx_department_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_department_id")
    private Department parentDepartment;

    @OneToMany(mappedBy = "parentDepartment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Department> subDepartments = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;
}
