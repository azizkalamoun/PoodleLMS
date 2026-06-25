package com.enterprise.poodle.repository;

import com.enterprise.poodle.entity.EmployeeNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface EmployeeNotificationRepository extends JpaRepository<EmployeeNotification, Long> {

    List<EmployeeNotification> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    Page<EmployeeNotification> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId, Pageable pageable);

    List<EmployeeNotification> findByEmployeeIdAndReadFalseOrderByCreatedAtDesc(Long employeeId);

    long countByEmployeeIdAndReadFalse(Long employeeId);

    @Modifying
    @Transactional
    @Query("UPDATE EmployeeNotification en SET en.read = true, en.readAt = CURRENT_TIMESTAMP WHERE en.id = :id")
    void markAsRead(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE EmployeeNotification en SET en.read = true, en.readAt = CURRENT_TIMESTAMP WHERE en.employee.id = :employeeId")
    void markAllAsRead(@Param("employeeId") Long employeeId);
}
