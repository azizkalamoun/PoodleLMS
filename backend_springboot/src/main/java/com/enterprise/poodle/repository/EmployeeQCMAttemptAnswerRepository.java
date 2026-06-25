package com.enterprise.poodle.repository;

import com.enterprise.poodle.entity.EmployeeQCMAttemptAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeQCMAttemptAnswerRepository extends JpaRepository<EmployeeQCMAttemptAnswer, Long> {
    List<EmployeeQCMAttemptAnswer> findByAttemptId(Long attemptId);
}
