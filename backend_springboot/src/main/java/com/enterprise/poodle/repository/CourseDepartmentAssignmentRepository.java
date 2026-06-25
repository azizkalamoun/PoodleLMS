package com.enterprise.poodle.repository;

import com.enterprise.poodle.entity.CourseDepartmentAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseDepartmentAssignmentRepository extends JpaRepository<CourseDepartmentAssignment, Long> {

    List<CourseDepartmentAssignment> findByCourseId(Long courseId);

    List<CourseDepartmentAssignment> findByDepartmentId(Long departmentId);

    Optional<CourseDepartmentAssignment> findByCourseIdAndDepartmentId(Long courseId, Long departmentId);

    boolean existsByCourseIdAndDepartmentId(Long courseId, Long departmentId);

    @Query("""
            SELECT cda FROM CourseDepartmentAssignment cda
            WHERE cda.course.id = :courseId
            AND cda.deadlineDate < CURRENT_DATE
            """)
    List<CourseDepartmentAssignment> findOverdueAssignments(@Param("courseId") Long courseId);
}
