package com.enterprise.poodle.entity;

import com.enterprise.poodle.enums.ContentType;
import com.enterprise.poodle.enums.QcmType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course_sections", indexes = {
        @Index(name = "idx_section_course", columnList = "course_id"),
        @Index(name = "idx_section_order", columnList = "course_id, order_index")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private ContentType contentType;

    @Column(name = "content_url", columnDefinition = "TEXT")
    private String contentUrl;

    @Column(name = "file_description", columnDefinition = "TEXT")
    private String fileDescription;

    @Column(name = "order_index", nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "qcm_type")
    private QcmType qcmType;

    @Column(name = "max_attempts")
    private Integer maxAttempts;

    @Column(name = "llm_draft_enabled", nullable = false)
    @Builder.Default
    private boolean llmDraftEnabled = false;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QCMQuestion> questions = new ArrayList<>();
}
