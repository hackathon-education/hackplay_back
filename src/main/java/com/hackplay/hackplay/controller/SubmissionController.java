package com.hackplay.hackplay.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.hackplay.hackplay.common.ApiErrorResponses;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.common.CommonResponse;
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

    @ApiErrorResponses({BaseResponseStatus.VALIDATION_ERROR, BaseResponseStatus.PROJECT_NOT_FOUND})
    @PostMapping
    public CommonResponse<Void> submitProject(@Valid @RequestBody SubmissionReqDto submissionReqDto, @AuthenticationPrincipal String uuid) throws IOException {
        submissionService.submit(uuid, submissionReqDto);
        return CommonResponse.success();
    }

    @GetMapping
    public CommonResponse<List<SubmissionListRespDto>> getMySubmissions(@AuthenticationPrincipal String uuid) {
        return CommonResponse.success(submissionService.getMySubmissions(uuid));
    }

    @ApiErrorResponses({BaseResponseStatus.NO_EXIST_SUBMISSION})
    @GetMapping("/{projectId}")
    public CommonResponse<SubmissionDetailRespDto> getSubmissionDetail(@PathVariable("projectId") Long projectId, @AuthenticationPrincipal String uuid) {
        return CommonResponse.success(submissionService.getSubmissionDetail(uuid, projectId));
    }
}
