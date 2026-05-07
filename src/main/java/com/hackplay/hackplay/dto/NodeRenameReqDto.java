package com.hackplay.hackplay.dto;

import com.hackplay.hackplay.common.enums.project.NodeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NodeRenameReqDto {

    @NotNull(message = "타입은 필수입니다. (FILE 또는 DIRECTORY)")
    private NodeType type;

    @NotBlank(message = "현재 경로는 필수입니다.")
    private String currentPath;

    @NotBlank(message = "새 이름은 필수입니다.")
    private String newName;
}
