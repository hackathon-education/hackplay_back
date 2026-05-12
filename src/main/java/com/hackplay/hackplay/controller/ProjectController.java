package com.hackplay.hackplay.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.hackplay.hackplay.common.ApiErrorResponses;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.common.CommonResponse;
import com.hackplay.hackplay.common.enums.lecture.Lecture;
import com.hackplay.hackplay.dto.LectureProgressRespDto;
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

    @ApiErrorResponses({BaseResponseStatus.VALIDATION_ERROR, BaseResponseStatus.PROJECT_ALREADY_EXISTS_FOR_LECTURE, BaseResponseStatus.PROJECT_CREATION_FAILED})
    @PostMapping
    public CommonResponse<Void> createProject(@Valid @RequestBody ProjectCreateReqDto projectCreateReqDto, @AuthenticationPrincipal String uuid) throws IOException, InterruptedException {
        projectService.create(uuid, projectCreateReqDto);
        return CommonResponse.success();
    }

    @ApiErrorResponses({BaseResponseStatus.PROJECT_NOT_FOUND})
    @GetMapping("/lectures/{lectureId}/progress")
    public CommonResponse<LectureProgressRespDto> getLectureProgress(@PathVariable("lectureId") int lectureId, @AuthenticationPrincipal String uuid) {
        Lecture lecture = Lecture.fromId(lectureId);
        return CommonResponse.success(projectService.getLectureProgress(uuid, lecture));
    }

    @GetMapping
    public CommonResponse<List<ProjectRespDto>> getProjects(@AuthenticationPrincipal String uuid) {
        return CommonResponse.success(projectService.getProjects(uuid));
    }

    @ApiErrorResponses({BaseResponseStatus.PROJECT_ACCESS_DENIED, BaseResponseStatus.PROJECT_NOT_FOUND})
    @GetMapping("/{projectId}")
    public CommonResponse<ProjectRespDto> getProject(@PathVariable("projectId") Long projectId, @AuthenticationPrincipal String uuid) {
        return CommonResponse.success(projectService.getProject(uuid, projectId));
    }

    @ApiErrorResponses({BaseResponseStatus.PROJECT_ACCESS_DENIED, BaseResponseStatus.PROJECT_NOT_FOUND})
    @PatchMapping("/{projectId}")
    public CommonResponse<Void> updateProject(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody ProjectUpdateReqDto projectUpdateReqDto) {
        projectService.update(projectId, projectUpdateReqDto);
        return CommonResponse.success();
    }

    @ApiErrorResponses({BaseResponseStatus.PROJECT_NOT_FOUND, BaseResponseStatus.PROJECT_DELETION_FAILED})
    @DeleteMapping("/{projectId}")
    public CommonResponse<Void> deleteProject(@PathVariable("projectId") Long projectId) {
        projectService.delete(projectId);
        return CommonResponse.success();
    }
}
