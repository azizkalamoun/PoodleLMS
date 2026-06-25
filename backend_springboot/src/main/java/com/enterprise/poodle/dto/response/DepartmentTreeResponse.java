package com.enterprise.poodle.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class DepartmentTreeResponse {
    private Long id;
    private String name;
    @Builder.Default
    private List<DepartmentTreeResponse> children = new ArrayList<>();
}
