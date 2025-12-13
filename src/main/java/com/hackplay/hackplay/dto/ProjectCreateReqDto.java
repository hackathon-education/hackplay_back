package com.hackplay.hackplay.dto;

import com.hackplay.hackplay.common.CommonEnums;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectCreateReqDto {

    @NotBlank(message = "프로젝트 이름은 필수입니다.")
    @Size(max = 50, message = "프로젝트 이름은 50자를 초과할 수 없습니다.")
    private String name;

    @Size(max = 255, message = "프로젝트 설명은 255자를 초과할 수 없습니다.")
    private String description;

    @NotBlank(message = "템플릿 타입은 필수입니다. (예: react-vite, spring-boot)")
    private String templateType;

    @NotNull(message = "공개 여부는 필수입니다.")
    private Boolean isPublic;

    @NotNull(message = "강의명은 필수입니다.")
    private CommonEnums.Lecture lecture;
}
