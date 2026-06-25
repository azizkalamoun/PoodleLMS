package com.enterprise.poodle.mapper;

import com.enterprise.poodle.dto.response.CourseResponse;
import com.enterprise.poodle.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    @Mapping(target = "prerequisiteCourseIds", ignore = true)
    CourseResponse toResponse(Course course);
}
