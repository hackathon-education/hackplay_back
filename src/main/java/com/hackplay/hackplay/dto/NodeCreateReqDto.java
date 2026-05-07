package com.hackplay.hackplay.dto;

import com.hackplay.hackplay.common.enums.project.NodeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NodeCreateReqDto {

    @NotNull(message = "타입은 필수입니다. (FILE 또는 DIRECTORY)")
    private NodeType type;

    @NotBlank(message = "이름은 비어 있을 수 없습니다.")
    @Size(max = 255, message = "이름은 255자를 초과할 수 없습니다.")
    private String name;

    private String parentPath;

    @Size(max = 1048576, message = "파일 크기는 1MB를 초과할 수 없습니다.")
    private String content;
}
