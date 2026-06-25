package com.enterprise.poodle.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employee_qcm_attempt_answers", indexes = {
        @Index(name = "idx_attempt_answer_attempt", columnList = "attempt_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeQCMAttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private EmployeeQCMAttempt attempt;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "selected_option")
    private String selectedOption;

    @Column(name = "correct", nullable = false)
    private boolean correct;
}
