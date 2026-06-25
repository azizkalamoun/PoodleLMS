package com.enterprise.poodle.repository;

import com.enterprise.poodle.entity.CoursePrerequisite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoursePrerequisiteRepository extends JpaRepository<CoursePrerequisite, Long> {

    @Query("SELECT cp.prerequisiteCourse.id FROM CoursePrerequisite cp WHERE cp.course.id = :courseId")
    List<Long> findPrerequisiteCourseIdsByCourseId(@Param("courseId") Long courseId);

    void deleteByCourseId(Long courseId);
}
