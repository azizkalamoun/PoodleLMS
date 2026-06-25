package com.enterprise.poodle.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String role;  // ROLE_ADMIN or ROLE_EMPLOYEE (with ROLE_ prefix)
    private String firstName;
    private String lastName;
}
