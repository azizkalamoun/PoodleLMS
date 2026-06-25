package com.enterprise.poodle.mapper;

import com.enterprise.poodle.dto.response.QCMQuestionResponse;
import com.enterprise.poodle.entity.QCMQuestion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QCMQuestionMapper {

    @Mapping(target = "correctOption", ignore = true)
    QCMQuestionResponse toResponse(QCMQuestion question);

    QCMQuestionResponse toResponseWithAnswer(QCMQuestion question);
}
