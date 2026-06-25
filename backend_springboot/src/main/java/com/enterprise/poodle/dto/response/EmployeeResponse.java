package com.enterprise.poodle.dto.response;

import com.enterprise.poodle.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private Long departmentId;
    private String departmentName;
}
