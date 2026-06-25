package com.enterprise.poodle.security;

import com.enterprise.poodle.entity.Employee;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public Employee getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Employee)) {
            throw new IllegalStateException("No authenticated employee found");
        }
        return (Employee) auth.getPrincipal();
    }

    public Long getCurrentEmployeeId() {
        return getCurrentEmployee().getId();
    }

    public boolean isAdmin() {
        return getCurrentEmployee().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
