package com.enterprise.poodle.repository;

import com.enterprise.poodle.entity.Notification;
import com.enterprise.poodle.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByType(NotificationType type);

    List<Notification> findByCreatedAtAfter(LocalDateTime dateTime);
}
