package com.enterprise.poodle.repository;

import com.enterprise.poodle.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByIdAndDeletedFalse(Long id);

    List<Department> findAllByDeletedFalse();

    Page<Department> findAllByDeletedFalse(Pageable pageable);

    @Query("SELECT d FROM Department d WHERE d.parentDepartment IS NULL AND d.deleted = false")
    List<Department> findRootDepartments();

    boolean existsByNameAndDeletedFalse(String name);
}
