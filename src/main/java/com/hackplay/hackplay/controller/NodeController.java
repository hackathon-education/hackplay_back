package com.hackplay.hackplay.controller;

import com.hackplay.hackplay.common.ApiErrorResponses;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.common.CommonResponse;
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

    @ApiErrorResponses({BaseResponseStatus.VALIDATION_ERROR, BaseResponseStatus.DUPLICATE_FILE_NAME, BaseResponseStatus.DUPLICATE_DIRECTORY_NAME, BaseResponseStatus.PROJECT_NOT_FOUND})
    @PostMapping
    public CommonResponse<Void> createNode(
            @AuthenticationPrincipal String uuid,
            @PathVariable Long projectId,
            @Valid @RequestBody NodeCreateReqDto dto) throws IOException {
        nodeService.create(uuid, projectId, dto);
        return CommonResponse.success();
    }

    @ApiErrorResponses({BaseResponseStatus.FILE_NOT_FOUND, BaseResponseStatus.PROJECT_NOT_FOUND})
    @GetMapping("/file/content")
    public CommonResponse<FileRespDto> getFileContent(
            @PathVariable Long projectId,
            @RequestParam("path") String path) throws IOException {
        return CommonResponse.success(nodeService.getFileContent(projectId, path));
    }

    @ApiErrorResponses({BaseResponseStatus.FILE_SIZE_EXCEEDED, BaseResponseStatus.FILE_NOT_FOUND})
    @PatchMapping("/file/content")
    public CommonResponse<Void> updateFileContent(
            @PathVariable Long projectId,
            @Valid @RequestBody FileUpdateReqDto dto) throws IOException {
        nodeService.updateFileContent(projectId, dto);
        return CommonResponse.success();
    }

    @ApiErrorResponses({BaseResponseStatus.DIRECTORY_NOT_FOUND})
    @GetMapping("/dir/tree")
    public CommonResponse<DirectoryTreeRespDto> getDirTree(
            @PathVariable Long projectId) {
        return CommonResponse.success(nodeService.getTree(projectId));
    }

    @ApiErrorResponses({BaseResponseStatus.VALIDATION_ERROR, BaseResponseStatus.FILE_NOT_FOUND, BaseResponseStatus.DIRECTORY_NOT_FOUND})
    @PatchMapping("/rename")
    public CommonResponse<Void> renameNode(
            @PathVariable Long projectId,
            @Valid @RequestBody NodeRenameReqDto dto) throws IOException {
        nodeService.rename(projectId, dto);
        return CommonResponse.success();
    }

    @ApiErrorResponses({BaseResponseStatus.VALIDATION_ERROR, BaseResponseStatus.FILE_NOT_FOUND, BaseResponseStatus.DIRECTORY_NOT_FOUND})
    @PatchMapping("/move")
    public CommonResponse<Void> moveNode(
            @PathVariable Long projectId,
            @Valid @RequestBody NodeMoveReqDto dto) throws IOException {
        nodeService.move(projectId, dto);
        return CommonResponse.success();
    }

    @ApiErrorResponses({BaseResponseStatus.VALIDATION_ERROR, BaseResponseStatus.FILE_NOT_FOUND, BaseResponseStatus.DIRECTORY_NOT_FOUND})
    @DeleteMapping
    public CommonResponse<Void> deleteNode(
            @PathVariable Long projectId,
            @RequestParam("path") String path,
            @RequestParam("type") NodeType type) throws IOException {
        nodeService.delete(projectId, path, type);
        return CommonResponse.success();
    }
}
