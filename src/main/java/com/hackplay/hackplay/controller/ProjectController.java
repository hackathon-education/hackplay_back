package com.hackplay.hackplay.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackplay.hackplay.common.ApiResponse;
import com.hackplay.hackplay.dto.ProjectCreateReqDto;
import com.hackplay.hackplay.dto.ProjectRespDto;
import com.hackplay.hackplay.dto.ProjectUpdateReqDto;
import com.hackplay.hackplay.service.ProjectService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ApiResponse<Void> createProject(@Valid @RequestBody ProjectCreateReqDto projectCreateReqDto) throws IOException, InterruptedException {
        projectService.create(projectCreateReqDto);
        return ApiResponse.success();
    }

    @GetMapping
    public ApiResponse<List<ProjectRespDto>> getProjects() {
        List<ProjectRespDto> projectRespDto = projectService.getProjects();
        return ApiResponse.success(projectRespDto);
    }

    @GetMapping("/{projectId}")
    public ApiResponse<ProjectRespDto> getProject(@PathVariable Long projectId) {
        ProjectRespDto projectRespDto = projectService.getProject(projectId);
        return ApiResponse.success(projectRespDto);
    }

    @PatchMapping("/{projectId}")
    public ApiResponse<Void> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectUpdateReqDto projectUpdateReqDto) {
        projectService.update(projectId, projectUpdateReqDto);
        return ApiResponse.success();
    }

    @DeleteMapping("/{projectId}")
    public ApiResponse<Void> deleteProject(@PathVariable Long projectId) {
        projectService.delete(projectId);
        return ApiResponse.success();
    }

    @PostMapping("/{projectId}/start")
    public ApiResponse<Void> startProject(@PathVariable Long projectId) {
        projectService.start(projectId);
        return ApiResponse.success();
    }

    @PostMapping("/{projectId}/stop")
    public ApiResponse<Void> stopProject(@PathVariable Long projectId) {
        projectService.stop(projectId);
        return ApiResponse.success();
    }
}
