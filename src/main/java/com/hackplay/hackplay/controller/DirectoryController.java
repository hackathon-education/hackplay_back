package com.hackplay.hackplay.controller;

import java.io.IOException;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.hackplay.hackplay.common.ApiResponse;
import com.hackplay.hackplay.dto.DirectoryCreateReqDto;
import com.hackplay.hackplay.dto.DirectoryTreeRespDto;
import com.hackplay.hackplay.dto.DirectoryMoveReqDto;
import com.hackplay.hackplay.dto.DirectoryRenameReqDto;
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
            @AuthenticationPrincipal String uuid,
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody DirectoryCreateReqDto directoryCreateReqDto) throws IOException {
        directoryService.create(uuid, projectId, directoryCreateReqDto);
        return ApiResponse.success();
    }

    @GetMapping("/tree")
    public ApiResponse<DirectoryTreeRespDto> viewDirTree(
            @PathVariable("projectId") Long projectId) {
        return ApiResponse.success(directoryService.view(projectId));
    }

    @PatchMapping("/rename")
    public ApiResponse<Void> renameDir(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody DirectoryRenameReqDto directoryRenameReqDto) throws IOException {
        directoryService.rename(projectId, directoryRenameReqDto);
        return ApiResponse.success();
    }

    @PatchMapping("/move")
    public ApiResponse<Void> moveDir(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody DirectoryMoveReqDto directoryMoveReqDto) throws IOException {
        directoryService.move(projectId, directoryMoveReqDto);
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
