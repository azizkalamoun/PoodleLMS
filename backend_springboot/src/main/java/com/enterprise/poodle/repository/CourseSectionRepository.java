package com.enterprise.poodle.repository;

import com.enterprise.poodle.entity.CourseSection;
import com.enterprise.poodle.enums.QcmType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {

    List<CourseSection> findByCourseIdOrderByOrderIndexAsc(Long courseId);

    @Query("SELECT cs FROM CourseSection cs WHERE cs.course.id = :courseId AND cs.qcmType = :qcmType")
    Optional<CourseSection> findByCourseIdAndQcmType(
            @Param("courseId") Long courseId,
            @Param("qcmType") QcmType qcmType);

    @Query("SELECT COUNT(cs) FROM CourseSection cs WHERE cs.course.id = :courseId")
    long countByCourseId(@Param("courseId") Long courseId);

    boolean existsByCourseIdAndQcmType(Long courseId, QcmType qcmType);
}
