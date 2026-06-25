package com.enterprise.poodle.service;

import com.enterprise.poodle.dto.request.LoginRequest;
import com.enterprise.poodle.dto.request.RegisterRequest;
import com.enterprise.poodle.dto.response.AuthResponse;
import com.enterprise.poodle.dto.response.EmployeeResponse;
import com.enterprise.poodle.entity.Department;
import com.enterprise.poodle.entity.Employee;
import com.enterprise.poodle.exception.DuplicateResourceException;
import com.enterprise.poodle.exception.ResourceNotFoundException;
import com.enterprise.poodle.mapper.EmployeeMapper;
import com.enterprise.poodle.repository.DepartmentRepository;
import com.enterprise.poodle.repository.EmployeeRepository;
import com.enterprise.poodle.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmployeeMapper employeeMapper;

    public AuthResponse login(LoginRequest request) {
        log.debug("🔍 [SERVICE] Starting authentication for: {}", request.getEmail());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            log.debug("✓ [SERVICE] Authentication successful, authenticated: {}", authentication.isAuthenticated());

            String token = jwtTokenProvider.generateToken(authentication);
            log.debug("✓ [SERVICE] JWT token generated");
            
            Employee employee = (Employee) authentication.getPrincipal();
            log.debug("✓ [SERVICE] Employee loaded - Name: {} {}, Role: {}", 
                    employee.getFirstName(), employee.getLastName(), employee.getRole());

            // Keep Role enum as-is (ROLE_ADMIN, ROLE_EMPLOYEE) for consistent frontend checks
            String roleStr = employee.getRole().name();
            log.info("[SERVICE] Login processed - Email: {}, Role: {}", 
                    employee.getEmail(), roleStr);

            AuthResponse response = AuthResponse.builder()
                    .token(token)
                    .email(employee.getEmail())
                    .role(roleStr)
                    .firstName(employee.getFirstName())
                    .lastName(employee.getLastName())
                    .build();
            
            log.debug("✓ [SERVICE] AuthResponse built: {}", response);
            return response;
        } catch (Exception e) {
            log.error(" [SERVICE] Authentication failed for {}: {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    @Transactional
    public EmployeeResponse register(RegisterRequest request) {
        log.info(" [SERVICE] Starting registration for: {}", request.getEmail());
        
        if (employeeRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            log.warn("  [SERVICE] Employee already exists: {}", request.getEmail());
            throw new DuplicateResourceException(
                    "Employee with email " + request.getEmail() + " already exists");
        }

        Employee employee = Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findByIdAndDeletedFalse(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department", "id", request.getDepartmentId()));
            employee.setDepartment(department);
            log.debug("✓ [SERVICE] Department assigned: {}", department.getName());
        }

        Employee saved = employeeRepository.save(employee);
        log.info("[SERVICE] Registration complete for: {}", request.getEmail());
        return employeeMapper.toResponse(saved);
    }
}
