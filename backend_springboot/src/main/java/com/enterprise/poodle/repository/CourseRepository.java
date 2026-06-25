package com.enterprise.poodle.repository;

import com.enterprise.poodle.entity.Course;
import com.enterprise.poodle.enums.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByIdAndDeletedFalse(Long id);

    Page<Course> findAllByDeletedFalse(Pageable pageable);

    @Query("""
            SELECT DISTINCT c FROM Course c
            JOIN CourseDepartmentAssignment cda ON cda.course = c
            WHERE cda.department.id = :departmentId
            AND c.status = :status
            AND c.deleted = false
            """)
    List<Course> findAssignedCoursesByDepartmentAndStatus(
            @Param("departmentId") Long departmentId,
            @Param("status") CourseStatus status);

    @Query("""
            SELECT DISTINCT c FROM Course c
            JOIN CourseDepartmentAssignment cda ON cda.course = c
            WHERE cda.department.id = :departmentId
            AND c.status = 'PUBLISHED'
            AND c.deleted = false
            """)
    Page<Course> findPublishedCoursesByDepartment(
            @Param("departmentId") Long departmentId,
            Pageable pageable);
}
