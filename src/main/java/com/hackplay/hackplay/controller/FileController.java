package com.hackplay.hackplay.controller;

import com.hackplay.hackplay.common.ApiResponse;
import com.hackplay.hackplay.dto.FileCreateReqDto;
import com.hackplay.hackplay.dto.FileUpdateReqDto;
import com.hackplay.hackplay.dto.FileRespDto;
import com.hackplay.hackplay.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/files")
public class FileController {

    private final FileService fileService;

    @PostMapping
    public ApiResponse<Void> createFile(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody FileCreateReqDto dto) throws IOException {
                fileService.create(projectId, dto);
        return ApiResponse.success();
    }

    @GetMapping("/{fileId}")
    public ApiResponse<FileRespDto> viewFile(
            @PathVariable("projectId") Long projectId,
            @PathVariable("fileId") Long fileId) throws IOException {
            FileRespDto fileRespDto = fileService.getFile(projectId, fileId);
        return ApiResponse.success(fileRespDto);
    }

    @PatchMapping("/{fileId}")
    public ApiResponse<Void> updateFile(
            @PathVariable("projectId") Long projectId,
            @PathVariable("fileId") Long fileId,
            @Valid @RequestBody FileUpdateReqDto dto) throws IOException {
                fileService.update(projectId, fileId, dto);
        return ApiResponse.success();
    }

    @DeleteMapping("/{fileId}")
    public ApiResponse<Void> deleteFile(
            @PathVariable("projectId") Long projectId,
            @PathVariable("fileId") Long fileId) throws IOException {
        fileService.delete(projectId, fileId);
        return ApiResponse.success();
    }
}
