package com.enterprise.poodle.service;

import com.enterprise.poodle.dto.request.DepartmentRequest;
import com.enterprise.poodle.dto.response.DepartmentResponse;
import com.enterprise.poodle.dto.response.DepartmentTreeResponse;
import com.enterprise.poodle.entity.Department;
import com.enterprise.poodle.exception.BusinessException;
import com.enterprise.poodle.exception.ResourceNotFoundException;
import com.enterprise.poodle.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        Department department = Department.builder()
                .name(request.getName())
                .build();

        if (request.getParentDepartmentId() != null) {
            Department parent = departmentRepository.findByIdAndDeletedFalse(request.getParentDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department", "id", request.getParentDepartmentId()));
            department.setParentDepartment(parent);
        }

        Department saved = departmentRepository.save(department);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<DepartmentResponse> getAllDepartments(Pageable pageable) {
        return departmentRepository.findAllByDeletedFalse(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<DepartmentTreeResponse> getDepartmentTree() {
        List<Department> roots = departmentRepository.findRootDepartments();
        return roots.stream()
                .map(this::buildTree)
                .collect(Collectors.toList());
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        department.setName(request.getName());

        if (request.getParentDepartmentId() != null) {
            if (request.getParentDepartmentId().equals(id)) {
                throw new BusinessException("Department cannot be its own parent");
            }
            Department parent = departmentRepository.findByIdAndDeletedFalse(request.getParentDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department", "id", request.getParentDepartmentId()));
            department.setParentDepartment(parent);
        } else {
            department.setParentDepartment(null);
        }

        Department saved = departmentRepository.save(department);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        department.setDeleted(true);
        softDeleteChildren(department);
        departmentRepository.save(department);
    }

    private void softDeleteChildren(Department parent) {
        for (Department child : parent.getSubDepartments()) {
            if (!child.isDeleted()) {
                child.setDeleted(true);
                softDeleteChildren(child);
            }
        }
    }

    private DepartmentTreeResponse buildTree(Department department) {
        List<DepartmentTreeResponse> children = department.getSubDepartments().stream()
                .filter(d -> !d.isDeleted())
                .map(this::buildTree)
                .collect(Collectors.toList());

        return DepartmentTreeResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .children(children)
                .build();
    }

    private DepartmentResponse mapToResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .parentDepartmentId(department.getParentDepartment() != null
                        ? department.getParentDepartment().getId() : null)
                .parentDepartmentName(department.getParentDepartment() != null
                        ? department.getParentDepartment().getName() : null)
                .build();
    }
}
