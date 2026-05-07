package com.hackplay.hackplay.controller;

import com.hackplay.hackplay.common.ApiResponse;
import com.hackplay.hackplay.common.enums.project.NodeType;
import com.hackplay.hackplay.dto.*;
import com.hackplay.hackplay.service.NodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/nodes")
public class NodeController {

    private final NodeService nodeService;

    @PostMapping
    public ApiResponse<Void> createNode(
            @AuthenticationPrincipal String uuid,
            @PathVariable Long projectId,
            @Valid @RequestBody NodeCreateReqDto dto) throws IOException {
        nodeService.create(uuid, projectId, dto);
        return ApiResponse.success();
    }

    @GetMapping("/file/content")
    public ApiResponse<FileRespDto> getFileContent(
            @PathVariable Long projectId,
            @RequestParam("path") String path) throws IOException {
        return ApiResponse.success(nodeService.getFileContent(projectId, path));
    }

    @PatchMapping("/file/content")
    public ApiResponse<Void> updateFileContent(
            @PathVariable Long projectId,
            @Valid @RequestBody FileUpdateReqDto dto) throws IOException {
        nodeService.updateFileContent(projectId, dto);
        return ApiResponse.success();
    }

    @GetMapping("/dir/tree")
    public ApiResponse<DirectoryTreeRespDto> getDirTree(
            @PathVariable Long projectId) {
        return ApiResponse.success(nodeService.getTree(projectId));
    }

    @PatchMapping("/rename")
    public ApiResponse<Void> renameNode(
            @PathVariable Long projectId,
            @Valid @RequestBody NodeRenameReqDto dto) throws IOException {
        nodeService.rename(projectId, dto);
        return ApiResponse.success();
    }

    @PatchMapping("/move")
    public ApiResponse<Void> moveNode(
            @PathVariable Long projectId,
            @Valid @RequestBody NodeMoveReqDto dto) throws IOException {
        nodeService.move(projectId, dto);
        return ApiResponse.success();
    }

    @DeleteMapping
    public ApiResponse<Void> deleteNode(
            @PathVariable Long projectId,
            @RequestParam("path") String path,
            @RequestParam("type") NodeType type) throws IOException {
        nodeService.delete(projectId, path, type);
        return ApiResponse.success();
    }
}
