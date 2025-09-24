package com.hackplay.hackplay.controller;

import com.hackplay.hackplay.common.ApiResponse;
import com.hackplay.hackplay.dto.FileCreateReqDto;
import com.hackplay.hackplay.dto.FileUpdateReqDto;
import com.hackplay.hackplay.dto.FileRespDto;
import com.hackplay.hackplay.service.FileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/files")
public class FileController {

    private final FileService fileService;

    @PostMapping
    public ApiResponse<Void> createFile(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody FileCreateReqDto fileCreateReqDto) throws IOException {
        fileService.create(projectId, fileCreateReqDto);
        return ApiResponse.success();
    }

    @GetMapping
    public ApiResponse<FileRespDto> viewFile(
            @PathVariable("projectId") Long projectId,
            @RequestParam("path") String path) throws IOException {
        FileRespDto fileRespDto = fileService.getFile(projectId, path);
        return ApiResponse.success(fileRespDto);
    }

    @PatchMapping
    public ApiResponse<Void> updateFile(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody FileUpdateReqDto fileUpdateReqDto) throws IOException {
        fileService.update(projectId, fileUpdateReqDto);
        return ApiResponse.success();
    }

    @DeleteMapping
    public ApiResponse<Void> deleteFile(
            @PathVariable("projectId") Long projectId,
            @RequestParam("path") String path) throws IOException {
        fileService.delete(projectId, path);
        return ApiResponse.success();
    }
}
