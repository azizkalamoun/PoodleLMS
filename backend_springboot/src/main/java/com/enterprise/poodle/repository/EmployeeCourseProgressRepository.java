package com.enterprise.poodle.repository;

import com.enterprise.poodle.entity.EmployeeCourseProgress;
import com.enterprise.poodle.enums.ProgressStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeCourseProgressRepository extends JpaRepository<EmployeeCourseProgress, Long> {

    List<EmployeeCourseProgress> findByEmployeeIdAndCourseId(Long employeeId, Long courseId);

    Optional<EmployeeCourseProgress> findByEmployeeIdAndSectionId(Long employeeId, Long sectionId);

    @Query("""
            SELECT COUNT(ecp) FROM EmployeeCourseProgress ecp
            WHERE ecp.employee.id = :employeeId
            AND ecp.course.id = :courseId
            AND ecp.status = :status
            """)
    long countByEmployeeIdAndCourseIdAndStatus(
            @Param("employeeId") Long employeeId,
            @Param("courseId") Long courseId,
            @Param("status") ProgressStatus status);

    @Query("""
            SELECT COUNT(DISTINCT ecp.employee.id) FROM EmployeeCourseProgress ecp
            WHERE ecp.course.id = :courseId
            AND ecp.status = 'COMPLETED'
            GROUP BY ecp.employee.id
            HAVING COUNT(ecp) = (SELECT COUNT(cs) FROM CourseSection cs WHERE cs.course.id = :courseId)
            """)
    Long countEmployeesWhoCompletedAllSections(@Param("courseId") Long courseId);
}
