package com.hackplay.hackplay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirectoryTreeRespDto {
    private String name;
    private String path;
    private String type;
    private List<DirectoryTreeRespDto> children;
}
