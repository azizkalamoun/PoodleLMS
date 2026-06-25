package com.enterprise.poodle.mapper;

import com.enterprise.poodle.dto.response.EmployeeResponse;
import com.enterprise.poodle.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(source = "department.id", target = "departmentId")
    @Mapping(source = "department.name", target = "departmentName")
    EmployeeResponse toResponse(Employee employee);
}
