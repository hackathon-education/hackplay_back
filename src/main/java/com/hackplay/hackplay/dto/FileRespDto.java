package com.hackplay.hackplay.dto;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

@Data
@Builder
public class FileRespDto {
    private String name;
    private String path;
    private long size;
    private String content;

    public static FileRespDto from(Path filePath, String content) throws IOException {
        return FileRespDto.builder()
                .name(filePath.getFileName().toString())
                .path(filePath.toString())
                .size(Files.size(filePath))
                .content(content)
                .build();
    }
}
