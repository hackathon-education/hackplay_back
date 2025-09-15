package com.hackplay.hackplay.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DirectoryTreeRespDto {
    private Long dirId;
    private String name;
    private String path;
    private Long parentId;
    private List<DirectoryTreeRespDto> children = new ArrayList<>();
}
