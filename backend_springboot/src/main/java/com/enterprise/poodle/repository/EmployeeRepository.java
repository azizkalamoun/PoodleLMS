package com.enterprise.poodle.repository;

import com.enterprise.poodle.entity.Employee;
import com.enterprise.poodle.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmailAndDeletedFalse(String email);

    Optional<Employee> findByIdAndDeletedFalse(Long id);

    Page<Employee> findAllByDeletedFalse(Pageable pageable);

    boolean existsByEmailAndDeletedFalse(String email);

    @Query("SELECT e FROM Employee e WHERE e.department.id = :departmentId AND e.deleted = false")
    List<Employee> findByDepartmentId(@Param("departmentId") Long departmentId);

    @Query("SELECT e FROM Employee e WHERE e.department.id IN :departmentIds AND e.deleted = false")
    List<Employee> findByDepartmentIdIn(@Param("departmentIds") List<Long> departmentIds);

    @Query("SELECT e FROM Employee e WHERE e.department.id = :departmentId AND e.deleted = false")
    Page<Employee> findByDepartmentIdAndDeletedFalse(@Param("departmentId") Long departmentId, Pageable pageable);
}
