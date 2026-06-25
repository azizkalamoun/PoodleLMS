package com.enterprise.poodle.service;

import com.enterprise.poodle.dto.request.EmployeeUpdateRequest;
import com.enterprise.poodle.dto.request.PasswordChangeRequest;
import com.enterprise.poodle.dto.response.EmployeeResponse;
import com.enterprise.poodle.entity.Department;
import com.enterprise.poodle.entity.Employee;
import com.enterprise.poodle.enums.Role;
import com.enterprise.poodle.exception.BusinessException;
import com.enterprise.poodle.exception.DuplicateResourceException;
import com.enterprise.poodle.exception.ResourceNotFoundException;
import com.enterprise.poodle.mapper.EmployeeMapper;
import com.enterprise.poodle.repository.DepartmentRepository;
import com.enterprise.poodle.repository.EmployeeRepository;
import com.enterprise.poodle.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private SecurityUtils securityUtils;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private Department department;
    private EmployeeResponse employeeResponse;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .id(1L)
                .name("Engineering")
                .build();

        employee = Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@poodle.com")
                .password("encodedPassword")
                .role(Role.ROLE_EMPLOYEE)
                .department(department)
                .build();

        employeeResponse = EmployeeResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@poodle.com")
                .role(Role.ROLE_EMPLOYEE)
                .departmentId(1L)
                .departmentName("Engineering")
                .build();
    }

    @Nested
    @DisplayName("getAllEmployees")
    class GetAllEmployees {
        @Test
        void shouldReturnPaginatedEmployees() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Employee> page = new PageImpl<>(List.of(employee), pageable, 1);

            when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(page);
            when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

            Page<EmployeeResponse> result = employeeService.getAllEmployees(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("john.doe@poodle.com");
            verify(employeeRepository).findAllByDeletedFalse(pageable);
        }
    }

    @Nested
    @DisplayName("getEmployeeById")
    class GetEmployeeById {
        @Test
        void shouldReturnEmployee_whenFound() {
            when(employeeRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(employee));
            when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

            EmployeeResponse result = employeeService.getEmployeeById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getFirstName()).isEqualTo("John");
        }

        @Test
        void shouldThrowNotFound_whenEmployeeDoesNotExist() {
            when(employeeRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.getEmployeeById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getCurrentProfile")
    class GetCurrentProfile {
        @Test
        void shouldReturnCurrentEmployeeProfile() {
            when(securityUtils.getCurrentEmployee()).thenReturn(employee);
            when(employeeMapper.toResponse(employee)).thenReturn(employeeResponse);

            EmployeeResponse result = employeeService.getCurrentProfile();

            assertThat(result.getEmail()).isEqualTo("john.doe@poodle.com");
        }
    }

    @Nested
    @DisplayName("updateCurrentProfile")
    class UpdateCurrentProfile {
        @Test
        void shouldUpdateFirstAndLastName() {
            EmployeeUpdateRequest request = new EmployeeUpdateRequest();
            request.setFirstName("Jane");
            request.setLastName("Smith");

            when(securityUtils.getCurrentEmployeeId()).thenReturn(1L);
            when(employeeRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(employee));
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
            when(employeeMapper.toResponse(any(Employee.class))).thenReturn(employeeResponse);

            EmployeeResponse result = employeeService.updateCurrentProfile(request);

            assertThat(result).isNotNull();
            verify(employeeRepository).save(employee);
        }

        @Test
        void shouldThrowDuplicateResource_whenEmailAlreadyExists() {
            EmployeeUpdateRequest request = new EmployeeUpdateRequest();
            request.setEmail("existing@poodle.com");

            when(securityUtils.getCurrentEmployeeId()).thenReturn(1L);
            when(employeeRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(employee));
            when(employeeRepository.existsByEmailAndDeletedFalse("existing@poodle.com")).thenReturn(true);

            assertThatThrownBy(() -> employeeService.updateCurrentProfile(request))
                    .isInstanceOf(DuplicateResourceException.class);
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {
        @Test
        void shouldChangePassword_whenCurrentPasswordIsCorrect() {
            PasswordChangeRequest request = new PasswordChangeRequest();
            request.setCurrentPassword("OldPass@123");
            request.setNewPassword("NewPass@456");

            when(securityUtils.getCurrentEmployeeId()).thenReturn(1L);
            when(employeeRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(employee));
            when(passwordEncoder.matches("OldPass@123", "encodedPassword")).thenReturn(true);
            when(passwordEncoder.encode("NewPass@456")).thenReturn("newEncodedPassword");

            employeeService.changePassword(request);

            verify(employeeRepository).save(employee);
            assertThat(employee.getPassword()).isEqualTo("newEncodedPassword");
        }

        @Test
        void shouldThrowBusinessException_whenCurrentPasswordIsWrong() {
            PasswordChangeRequest request = new PasswordChangeRequest();
            request.setCurrentPassword("WrongPass");
            request.setNewPassword("NewPass@456");

            when(securityUtils.getCurrentEmployeeId()).thenReturn(1L);
            when(employeeRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(employee));
            when(passwordEncoder.matches("WrongPass", "encodedPassword")).thenReturn(false);

            assertThatThrownBy(() -> employeeService.changePassword(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Current password is incorrect");
        }
    }

    @Nested
    @DisplayName("updateEmployee (admin)")
    class UpdateEmployee {
        @Test
        void shouldUpdateEmployeeWithDepartment() {
            EmployeeUpdateRequest request = new EmployeeUpdateRequest();
            request.setFirstName("Updated");
            request.setDepartmentId(2L);

            Department newDept = Department.builder().id(2L).name("Marketing").build();

            when(employeeRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(employee));
            when(departmentRepository.findByIdAndDeletedFalse(2L)).thenReturn(Optional.of(newDept));
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
            when(employeeMapper.toResponse(any(Employee.class))).thenReturn(employeeResponse);

            EmployeeResponse result = employeeService.updateEmployee(1L, request);

            assertThat(result).isNotNull();
            verify(departmentRepository).findByIdAndDeletedFalse(2L);
            verify(employeeRepository).save(employee);
        }
    }

    @Nested
    @DisplayName("deleteEmployee")
    class DeleteEmployee {
        @Test
        void shouldSoftDeleteEmployee() {
            when(employeeRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(employee));

            employeeService.deleteEmployee(1L);

            assertThat(employee.isDeleted()).isTrue();
            verify(employeeRepository).save(employee);
        }

        @Test
        void shouldThrowNotFound_whenEmployeeDoesNotExist() {
            when(employeeRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.deleteEmployee(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
