package com.enterprise.poodle.service;

import com.enterprise.poodle.dto.request.EmployeeUpdateRequest;
import com.enterprise.poodle.dto.request.PasswordChangeRequest;
import com.enterprise.poodle.dto.request.RegisterRequest;
import com.enterprise.poodle.dto.response.EmployeeResponse;
import com.enterprise.poodle.entity.Department;
import com.enterprise.poodle.entity.Employee;
import com.enterprise.poodle.exception.BusinessException;
import com.enterprise.poodle.exception.DuplicateResourceException;
import com.enterprise.poodle.exception.ResourceNotFoundException;
import com.enterprise.poodle.mapper.EmployeeMapper;
import com.enterprise.poodle.repository.DepartmentRepository;
import com.enterprise.poodle.repository.EmployeeRepository;
import com.enterprise.poodle.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final SecurityUtils securityUtils;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeMapper employeeMapper;

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAllByDeletedFalse(pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public EmployeeResponse createEmployee(RegisterRequest request) {
        // Check if employee already exists
        if (employeeRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Employee with email " + request.getEmail() + " already exists");
        }

        // Build employee entity
        Employee employee = Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .deleted(false)
                .build();

        // Assign department if provided
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findByIdAndDeletedFalse(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department", "id", request.getDepartmentId()));
            employee.setDepartment(department);
        }

        Employee saved = employeeRepository.save(employee);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getCurrentProfile() {
        Employee employee = securityUtils.getCurrentEmployee();
        return mapToResponse(employee);
    }

    @Transactional
    public EmployeeResponse updateCurrentProfile(EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(
                        securityUtils.getCurrentEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id",
                        securityUtils.getCurrentEmployeeId()));

        if (request.getFirstName() != null) {
            employee.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            employee.setLastName(request.getLastName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(employee.getEmail())) {
            if (employeeRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
                throw new DuplicateResourceException(
                        "Employee with email " + request.getEmail() + " already exists");
            }
            employee.setEmail(request.getEmail());
        }
        // Ignore departmentId for self-update (only admin can change department)
        Employee saved = employeeRepository.save(employee);
        return mapToResponse(saved);
    }

    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(
                        securityUtils.getCurrentEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id",
                        securityUtils.getCurrentEmployeeId()));

        if (!passwordEncoder.matches(request.getCurrentPassword(), employee.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }
        employee.setPassword(passwordEncoder.encode(request.getNewPassword()));
        employeeRepository.save(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return mapToResponse(employee);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        if (request.getFirstName() != null) {
            employee.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            employee.setLastName(request.getLastName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(employee.getEmail())) {
            if (employeeRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
                throw new DuplicateResourceException(
                        "Employee with email " + request.getEmail() + " already exists");
            }
            employee.setEmail(request.getEmail());
        }
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findByIdAndDeletedFalse(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department", "id", request.getDepartmentId()));
            employee.setDepartment(department);
        }

        Employee saved = employeeRepository.save(employee);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getEmployeesByDepartment(Long departmentId, Pageable pageable) {
        // Verify department exists
        departmentRepository.findByIdAndDeletedFalse(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));

        // Get employees in department with pagination
        return employeeRepository.findByDepartmentIdAndDeletedFalse(departmentId, pageable)
                .map(this::mapToResponse);
    }

    private EmployeeResponse mapToResponse(Employee employee) {
        return employeeMapper.toResponse(employee);
    }
}
