package com.hackplay.hackplay.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.hackplay.hackplay.common.ApiResponse;
import com.hackplay.hackplay.common.enums.lecture.Lecture;
import com.hackplay.hackplay.dto.LectureProgressRespDto;
import com.hackplay.hackplay.dto.ProjectCreateReqDto;
import com.hackplay.hackplay.dto.ProjectRespDto;
import com.hackplay.hackplay.dto.ProjectUpdateReqDto;
import com.hackplay.hackplay.service.ProjectService;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 해당 강의에 대한 프로젝트가 존재합니다."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "프로젝트 생성에 실패했습니다.")
    })
    @PostMapping
    public ApiResponse<Void> createProject(@Valid @RequestBody ProjectCreateReqDto projectCreateReqDto, @AuthenticationPrincipal String uuid) throws IOException, InterruptedException {
        projectService.create(uuid, projectCreateReqDto);
        return ApiResponse.success();
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없습니다.")
    @GetMapping("/lectures/{lectureId}/progress")
    public ApiResponse<LectureProgressRespDto> getLectureProgress(@PathVariable("lectureId") int lectureId, @AuthenticationPrincipal String uuid) {
        Lecture lecture = Lecture.fromId(lectureId);
        return ApiResponse.success(projectService.getLectureProgress(uuid, lecture));
    }

    @GetMapping
    public ApiResponse<List<ProjectRespDto>> getProjects(@AuthenticationPrincipal String uuid) {
        return ApiResponse.success(projectService.getProjects(uuid));
    }

    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 프로젝트에 대한 접근 권한이 없습니다."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없습니다.")
    })
    @GetMapping("/{projectId}")
    public ApiResponse<ProjectRespDto> getProject(@PathVariable("projectId") Long projectId, @AuthenticationPrincipal String uuid) {
        return ApiResponse.success(projectService.getProject(uuid, projectId));
    }

    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 프로젝트에 대한 접근 권한이 없습니다."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없습니다.")
    })
    @PatchMapping("/{projectId}")
    public ApiResponse<Void> updateProject(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody ProjectUpdateReqDto projectUpdateReqDto) {
        projectService.update(projectId, projectUpdateReqDto);
        return ApiResponse.success();
    }

    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없습니다."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "프로젝트 삭제에 실패했습니다.")
    })
    @DeleteMapping("/{projectId}")
    public ApiResponse<Void> deleteProject(@PathVariable("projectId") Long projectId) {
        projectService.delete(projectId);
        return ApiResponse.success();
    }
}
