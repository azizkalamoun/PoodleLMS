package com.enterprise.poodle.repository;

import com.enterprise.poodle.entity.Certificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    Optional<Certificate> findByCertificateCode(String certificateCode);

    List<Certificate> findByEmployeeId(Long employeeId);

    Page<Certificate> findByEmployeeId(Long employeeId, Pageable pageable);

    boolean existsByEmployeeIdAndCourseId(Long employeeId, Long courseId);

    @Query("SELECT DISTINCT c FROM Certificate c LEFT JOIN FETCH c.employee LEFT JOIN FETCH c.course")
    Page<Certificate> findAllWithEagerLoading(Pageable pageable);
}
