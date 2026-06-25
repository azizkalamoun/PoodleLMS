package com.enterprise.poodle.repository;

import com.enterprise.poodle.entity.EmployeeQCMAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeQCMAttemptRepository extends JpaRepository<EmployeeQCMAttempt, Long> {

    List<EmployeeQCMAttempt> findByEmployeeIdAndSectionIdOrderByAttemptNumberAsc(
            Long employeeId, Long sectionId);

    @Query("""
            SELECT COUNT(a) FROM EmployeeQCMAttempt a
            WHERE a.employee.id = :employeeId AND a.section.id = :sectionId
            """)
    long countByEmployeeIdAndSectionId(
            @Param("employeeId") Long employeeId,
            @Param("sectionId") Long sectionId);

    @Query("""
            SELECT MAX(a.attemptNumber) FROM EmployeeQCMAttempt a
            WHERE a.employee.id = :employeeId AND a.section.id = :sectionId
            """)
    Integer findMaxAttemptNumber(
            @Param("employeeId") Long employeeId,
            @Param("sectionId") Long sectionId);

    @Query("""
            SELECT COUNT(a) FROM EmployeeQCMAttempt a
            WHERE a.section.id = :sectionId
            """)
    long countBySectionId(@Param("sectionId") Long sectionId);

    @Query("""
            SELECT COUNT(a) FROM EmployeeQCMAttempt a
            WHERE a.section.id = :sectionId AND a.score < :passingScore
            """)
    long countFailedBySectionId(
            @Param("sectionId") Long sectionId,
            @Param("passingScore") int passingScore);
}
