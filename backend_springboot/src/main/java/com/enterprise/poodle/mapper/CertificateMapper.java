package com.enterprise.poodle.mapper;

import com.enterprise.poodle.dto.response.CertificateResponse;
import com.enterprise.poodle.entity.Certificate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CertificateMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", expression = "java(certificate.getEmployee().getFirstName() + \" \" + certificate.getEmployee().getLastName())")
    @Mapping(target = "courseId", source = "course.id")
    @Mapping(source = "course.title", target = "courseTitle")
    @Mapping(source = "certificateCode", target = "certificateCode")
    @Mapping(source = "certificateCode", target = "verificationCode")
    @Mapping(source = "qrCodeUrl", target = "qrCodeUrl")
    @Mapping(source = "issuedAt", target = "issuedAt")
    @Mapping(source = "revoked", target = "revoked")
    @Mapping(target = "score", constant = "100")
    CertificateResponse toResponse(Certificate certificate);
}
