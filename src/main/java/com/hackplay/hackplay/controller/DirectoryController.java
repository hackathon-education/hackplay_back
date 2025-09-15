package com.hackplay.hackplay.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        @PathVariable Long projectId, 
        @Valid @RequestBody DirectoryCreateReqDto directoryCreateReqDto){
            directoryService.create(projectId, directoryCreateReqDto);
        return ApiResponse.success();
    }

    @GetMapping("/{dirId}/tree")
    public ApiResponse<DirectoryTreeRespDto> viewDirTree(
        @PathVariable Long projectId, 
        @PathVariable Long dirId){
            directoryService.view(projectId, dirId);
        return null;
    }

    @PatchMapping("/{dirId}")
    public ApiResponse<Void> updateName(
        @PathVariable Long projectId, 
        @PathVariable Long dirId,  
        @Valid @RequestBody DirectoryUpdateReqDto directoryUpdateReqDto){
            directoryService.update(projectId, dirId, directoryUpdateReqDto);
        return ApiResponse.success();
    }

    @DeleteMapping("/{dirId}")
    public ApiResponse<Void> deleteDir(
        @PathVariable Long projectId, 
        @PathVariable Long dirId){
            directoryService.delete(projectId, dirId);
        return ApiResponse.success();
    }
}
