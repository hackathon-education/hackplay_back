package com.hackplay.hackplay.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackplay.hackplay.common.ApiResponse;
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

    // 프로젝트 생성
    @PostMapping
    public ApiResponse<Void> createProject(@Valid @RequestBody ProjectCreateReqDto projectCreateReqDto, @AuthenticationPrincipal String uuid) throws IOException, InterruptedException {
        projectService.create(uuid, projectCreateReqDto);
        return ApiResponse.success();
    }

    // 강의 진행도 조회
    @GetMapping("/lectures/{lectureId}/progress")
    public ApiResponse<LectureProgressRespDto> getLectureProgress(@PathVariable("lectureId") int lectureId, @AuthenticationPrincipal String uuid) {
        Lecture lecture = Lecture.fromId(lectureId);
        return ApiResponse.success(projectService.getLectureProgress(uuid, lecture));
    }

    // 프로젝트 목록 조회
    @GetMapping
    public ApiResponse<List<ProjectRespDto>> getProjects(@AuthenticationPrincipal String uuid) {
        List<ProjectRespDto> projectRespDto = projectService.getProjects(uuid);
        return ApiResponse.success(projectRespDto);
    }

    // 프로젝트 상세 조회
    @GetMapping("/{projectId}")
    public ApiResponse<ProjectRespDto> getProject(@PathVariable("projectId") Long projectId, @AuthenticationPrincipal String uuid) {
        ProjectRespDto projectRespDto = projectService.getProject(uuid, projectId);
        return ApiResponse.success(projectRespDto);
    }

    // 프로젝트 수정
    @PatchMapping("/{projectId}")
    public ApiResponse<Void> updateProject(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody ProjectUpdateReqDto projectUpdateReqDto) {
        projectService.update(projectId, projectUpdateReqDto);
        return ApiResponse.success();
    }

    // 프로젝트 삭제
    @DeleteMapping("/{projectId}")
    public ApiResponse<Void> deleteProject(@PathVariable("projectId") Long projectId) {
        projectService.delete(projectId);
        return ApiResponse.success();
    }
}
