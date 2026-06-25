package com.enterprise.poodle.service;

import com.enterprise.poodle.dto.request.CourseRequest;
import com.enterprise.poodle.dto.response.CourseResponse;
import com.enterprise.poodle.entity.Course;
import com.enterprise.poodle.entity.Department;
import com.enterprise.poodle.entity.Employee;
import com.enterprise.poodle.enums.CourseStatus;
import com.enterprise.poodle.enums.Role;
import com.enterprise.poodle.exception.ResourceNotFoundException;
import com.enterprise.poodle.mapper.CourseMapper;
import com.enterprise.poodle.repository.*;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private CoursePrerequisiteRepository coursePrerequisiteRepository;
    @Mock private CourseDepartmentAssignmentRepository assignmentRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private EmployeeCourseGradeRepository gradeRepository;
    @Mock private SecurityUtils securityUtils;
    @Mock private AuditLogService auditLogService;
    @Mock private CourseMapper courseMapper;

    @InjectMocks
    private CourseService courseService;

    private Course course;
    private CourseResponse courseResponse;

    @BeforeEach
    void setUp() {
        course = Course.builder()
                .id(1L)
                .title("Spring Boot Fundamentals")
                .description("Learn Spring Boot basics")
                .status(CourseStatus.PUBLISHED)
                .passingScore(70)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        courseResponse = CourseResponse.builder()
                .id(1L)
                .title("Spring Boot Fundamentals")
                .description("Learn Spring Boot basics")
                .status(CourseStatus.PUBLISHED)
                .passingScore(70)
                .build();
    }

    @Nested
    @DisplayName("createCourse")
    class CreateCourse {
        @Test
        void shouldCreateCourseWithDefaults() {
            CourseRequest request = new CourseRequest();
            request.setTitle("New Course");
            request.setDescription("Description");

            when(courseRepository.save(any(Course.class))).thenReturn(course);
            when(courseMapper.toResponse(any(Course.class))).thenReturn(courseResponse);
            when(coursePrerequisiteRepository.findPrerequisiteCourseIdsByCourseId(anyLong()))
                    .thenReturn(List.of());

            CourseResponse result = courseService.createCourse(request);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Spring Boot Fundamentals");
            verify(courseRepository).save(any(Course.class));
            verify(auditLogService).logAction(any(), eq("Course"), anyLong(), any(), any());
        }
    }

    @Nested
    @DisplayName("getAllCourses")
    class GetAllCourses {
        @Test
        void shouldReturnPaginatedCourses() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Course> page = new PageImpl<>(List.of(course), pageable, 1);

            when(courseRepository.findAllByDeletedFalse(pageable)).thenReturn(page);
            when(courseMapper.toResponse(course)).thenReturn(courseResponse);
            when(coursePrerequisiteRepository.findPrerequisiteCourseIdsByCourseId(anyLong()))
                    .thenReturn(List.of());

            Page<CourseResponse> result = courseService.getAllCourses(pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(courseRepository).findAllByDeletedFalse(pageable);
        }
    }

    @Nested
    @DisplayName("getCourseById")
    class GetCourseById {
        @Test
        void shouldReturnCourse_whenFoundAndAdmin() {
            when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(course));
            when(securityUtils.isAdmin()).thenReturn(true);
            when(courseMapper.toResponse(course)).thenReturn(courseResponse);
            when(coursePrerequisiteRepository.findPrerequisiteCourseIdsByCourseId(1L))
                    .thenReturn(List.of());

            CourseResponse result = courseService.getCourseById(1L);

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        void shouldThrowNotFound_whenCourseDoesNotExist() {
            when(courseRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.getCourseById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateCourse")
    class UpdateCourse {
        @Test
        void shouldUpdateCourseFields() {
            CourseRequest request = new CourseRequest();
            request.setTitle("Updated Title");

            when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(course));
            when(courseRepository.save(any(Course.class))).thenReturn(course);
            when(courseMapper.toResponse(any(Course.class))).thenReturn(courseResponse);
            when(coursePrerequisiteRepository.findPrerequisiteCourseIdsByCourseId(anyLong()))
                    .thenReturn(List.of());

            CourseResponse result = courseService.updateCourse(1L, request);

            assertThat(result).isNotNull();
            verify(courseRepository).save(course);
            verify(auditLogService).logAction(any(), eq("Course"), anyLong(), any(), any());
        }
    }

    @Nested
    @DisplayName("deleteCourse")
    class DeleteCourse {
        @Test
        void shouldSoftDeleteCourse() {
            when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(course));

            courseService.deleteCourse(1L);

            assertThat(course.isDeleted()).isTrue();
            verify(courseRepository).save(course);
            verify(auditLogService).logAction(any(), eq("Course"), eq(1L), any(), any());
        }

        @Test
        void shouldThrowNotFound_whenCourseDoesNotExist() {
            when(courseRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.deleteCourse(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("unassignCourseFromDepartment")
    class UnassignCourse {
        @Test
        void shouldThrowNotFound_whenCourseDoesNotExist() {
            when(courseRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> courseService.unassignCourseFromDepartment(999L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
