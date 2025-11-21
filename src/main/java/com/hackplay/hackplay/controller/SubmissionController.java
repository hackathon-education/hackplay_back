package com.hackplay.hackplay.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackplay.hackplay.common.ApiResponse;
import com.hackplay.hackplay.dto.SubmissionDetailRespDto;
import com.hackplay.hackplay.dto.SubmissionListRespDto;
import com.hackplay.hackplay.dto.SubmissionReqDto;
import com.hackplay.hackplay.service.SubmissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    // 코드 제출
    @PostMapping
    public ApiResponse<Void> submitProject(@Valid @RequestBody SubmissionReqDto submissionReqDto) throws IOException {
        submissionService.submit(submissionReqDto);
        return ApiResponse.success();
    }

    // 내 제출 목록 조회
    @GetMapping("/my")
    public ApiResponse<List<SubmissionListRespDto>> getMySubmissions() {
        return ApiResponse.success(submissionService.getMySubmissions());
    }

    // 내 제출 상세 조회 (코드 + 프로젝트 정보)
    @GetMapping("/{submissionId}")
    public ApiResponse<SubmissionDetailRespDto> getSubmissionDetail(@PathVariable("submissionId") Long submissionId) {
        return ApiResponse.success(submissionService.getSubmissionDetail(submissionId));
    }
}
