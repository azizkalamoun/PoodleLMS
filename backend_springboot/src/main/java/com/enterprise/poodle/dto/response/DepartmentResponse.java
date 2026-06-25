package com.enterprise.poodle.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DepartmentResponse {
    private Long id;
    private String name;
    private Long parentDepartmentId;
    private String parentDepartmentName;
}
