package com.hackplay.hackplay.controller;

import com.hackplay.hackplay.common.ApiResponse;
import com.hackplay.hackplay.common.enums.project.NodeType;
import com.hackplay.hackplay.dto.*;
import com.hackplay.hackplay.service.NodeService;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검증 실패 또는 이미 존재하는 이름입니다."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없습니다.")
    })
    @PostMapping
    public ApiResponse<Void> createNode(
            @AuthenticationPrincipal String uuid,
            @PathVariable Long projectId,
            @Valid @RequestBody NodeCreateReqDto dto) throws IOException {
        nodeService.create(uuid, projectId, dto);
        return ApiResponse.success();
    }

    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "파일 또는 프로젝트를 찾을 수 없습니다.")
    })
    @GetMapping("/file/content")
    public ApiResponse<FileRespDto> getFileContent(
            @PathVariable Long projectId,
            @RequestParam("path") String path) throws IOException {
        return ApiResponse.success(nodeService.getFileContent(projectId, path));
    }

    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "파일 크기가 1MB를 초과했습니다."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "파일을 찾을 수 없습니다.")
    })
    @PatchMapping("/file/content")
    public ApiResponse<Void> updateFileContent(
            @PathVariable Long projectId,
            @Valid @RequestBody FileUpdateReqDto dto) throws IOException {
        nodeService.updateFileContent(projectId, dto);
        return ApiResponse.success();
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "디렉토리를 찾을 수 없습니다.")
    @GetMapping("/dir/tree")
    public ApiResponse<DirectoryTreeRespDto> getDirTree(
            @PathVariable Long projectId) {
        return ApiResponse.success(nodeService.getTree(projectId));
    }

    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 파일 또는 디렉토리를 찾을 수 없습니다.")
    })
    @PatchMapping("/rename")
    public ApiResponse<Void> renameNode(
            @PathVariable Long projectId,
            @Valid @RequestBody NodeRenameReqDto dto) throws IOException {
        nodeService.rename(projectId, dto);
        return ApiResponse.success();
    }

    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 파일 또는 디렉토리를 찾을 수 없습니다.")
    })
    @PatchMapping("/move")
    public ApiResponse<Void> moveNode(
            @PathVariable Long projectId,
            @Valid @RequestBody NodeMoveReqDto dto) throws IOException {
        nodeService.move(projectId, dto);
        return ApiResponse.success();
    }

    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 파일 또는 디렉토리를 찾을 수 없습니다.")
    })
    @DeleteMapping
    public ApiResponse<Void> deleteNode(
            @PathVariable Long projectId,
            @RequestParam("path") String path,
            @RequestParam("type") NodeType type) throws IOException {
        nodeService.delete(projectId, path, type);
        return ApiResponse.success();
    }
}
