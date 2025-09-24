package com.hackplay.hackplay.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.*;

import com.hackplay.hackplay.common.ApiResponse;
import com.hackplay.hackplay.dto.DirectoryCreateReqDto;
import com.hackplay.hackplay.dto.DirectoryTreeRespDto;
import com.hackplay.hackplay.dto.DirectoryUpdateReqDto;
import com.hackplay.hackplay.service.DirectoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/dirs")
public class DirectoryController {

    private final DirectoryService directoryService;

    @PostMapping
    public ApiResponse<Void> createDir(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody DirectoryCreateReqDto directoryCreateReqDto) throws IOException {
        directoryService.create(projectId, directoryCreateReqDto);
        return ApiResponse.success();
    }

    @GetMapping("/tree")
    public ApiResponse<DirectoryTreeRespDto> viewDirTree(
            @PathVariable("projectId") Long projectId) {
        return ApiResponse.success(directoryService.view(projectId));
    }

    @PatchMapping
    public ApiResponse<Void> updateDir(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody DirectoryUpdateReqDto directoryUpdateReqDto) throws IOException {
        directoryService.update(projectId, directoryUpdateReqDto);
        return ApiResponse.success();
    }

    @DeleteMapping
    public ApiResponse<Void> deleteDir(
            @PathVariable("projectId") Long projectId,
            @RequestParam("path") String dirPath) throws IOException {
        directoryService.delete(projectId, dirPath);
        return ApiResponse.success();
    }
}
