package com.enterprise.poodle.repository;

import com.enterprise.poodle.entity.EmployeeCourseGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeCourseGradeRepository extends JpaRepository<EmployeeCourseGrade, Long> {

    Optional<EmployeeCourseGrade> findByEmployeeIdAndCourseId(Long employeeId, Long courseId);

    @Query("""
            SELECT COUNT(g) FROM EmployeeCourseGrade g
            WHERE g.course.id = :courseId AND g.passed = true
            """)
    long countPassedByCourseId(@Param("courseId") Long courseId);

    @Query("""
            SELECT COUNT(g) FROM EmployeeCourseGrade g
            WHERE g.course.id = :courseId
            """)
    long countByCourseId(@Param("courseId") Long courseId);

    @Query("""
            SELECT AVG(g.finalScore) FROM EmployeeCourseGrade g
            WHERE g.employee.department.id = :departmentId
            """)
    Double findAverageScoreByDepartmentId(@Param("departmentId") Long departmentId);

    @Query("""
            SELECT COUNT(g) FROM EmployeeCourseGrade g
            WHERE g.employee.department.id = :departmentId
            """)
    long countByDepartmentId(@Param("departmentId") Long departmentId);

    boolean existsByEmployeeIdAndCourseIdAndPassedTrue(Long employeeId, Long courseId);
}
