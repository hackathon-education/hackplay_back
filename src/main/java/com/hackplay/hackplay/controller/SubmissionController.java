package com.hackplay.hackplay.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.hackplay.hackplay.common.ApiResponse;
import com.hackplay.hackplay.dto.SubmissionDetailRespDto;
import com.hackplay.hackplay.dto.SubmissionListRespDto;
import com.hackplay.hackplay.dto.SubmissionReqDto;
import com.hackplay.hackplay.service.SubmissionService;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "현재 제출할 수 없는 상태입니다."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없습니다.")
    })
    @PostMapping
    public ApiResponse<Void> submitProject(@Valid @RequestBody SubmissionReqDto submissionReqDto, @AuthenticationPrincipal String uuid) throws IOException {
        submissionService.submit(uuid, submissionReqDto);
        return ApiResponse.success();
    }

    @GetMapping
    public ApiResponse<List<SubmissionListRespDto>> getMySubmissions(@AuthenticationPrincipal String uuid) {
        return ApiResponse.success(submissionService.getMySubmissions(uuid));
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "제출 내역이 존재하지 않습니다.")
    @GetMapping("/{projectId}")
    public ApiResponse<SubmissionDetailRespDto> getSubmissionDetail(@PathVariable("projectId") Long projectId, @AuthenticationPrincipal String uuid) {
        return ApiResponse.success(submissionService.getSubmissionDetail(uuid, projectId));
    }
}
