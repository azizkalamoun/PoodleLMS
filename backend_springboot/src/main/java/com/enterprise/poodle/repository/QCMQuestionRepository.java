package com.enterprise.poodle.repository;

import com.enterprise.poodle.entity.QCMQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QCMQuestionRepository extends JpaRepository<QCMQuestion, Long> {

    List<QCMQuestion> findBySectionId(Long sectionId);

    long countBySectionId(Long sectionId);
}
