package com.hackplay.hackplay.dto;

import com.hackplay.hackplay.domain.Directory;
import com.hackplay.hackplay.domain.File;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileRespDto {
    private Long fileId;
    private String name;
    private String path;
    private Directory dir;
    private Long size;
    private String content;
    private String createdAt;
    private String updatedAt;

    public static FileRespDto entityToDto(File file, String content) {
        return FileRespDto.builder()
                .fileId(file.getId())
                .name(file.getName())
                .path(file.getPath())
                .dir(file.getDir())
                .size(file.getSize())
                .content(content)
                .createdAt(file.getCreatedAt().toString())
                .updatedAt(file.getUpdatedAt().toString())
                .build();
    }
}
