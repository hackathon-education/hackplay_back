package com.hackplay.hackplay.controller;

import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.hackplay.hackplay.common.ApiErrorResponses;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.common.CommonResponse;
import com.hackplay.hackplay.dto.AdminGradeReqDto;
import com.hackplay.hackplay.dto.AdminSubmissionDetailRespDto;
import com.hackplay.hackplay.dto.AdminSubmissionListRespDto;
import com.hackplay.hackplay.service.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.io.File;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public CommonResponse<List<AdminSubmissionListRespDto>> getAllSubmissions() {
        return CommonResponse.success(adminService.getAllSubmissions());
    }

    @ApiErrorResponses({BaseResponseStatus.NO_EXIST_SUBMISSION})
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{submissionId}")
    public CommonResponse<AdminSubmissionDetailRespDto> getSubmissionDetail(@PathVariable("submissionId") Long submissionId) {
        return CommonResponse.success(adminService.getSubmissionDetail(submissionId));
    }

    @ApiErrorResponses({BaseResponseStatus.VALIDATION_ERROR, BaseResponseStatus.NO_EXIST_SUBMISSION})
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{submissionId}/grade")
    public CommonResponse<String> gradeSubmission(
            @PathVariable("submissionId") Long submissionId,
            @Valid @RequestBody AdminGradeReqDto adminGradeReqDto) {
        adminService.grade(submissionId, adminGradeReqDto.getStatus());
        return CommonResponse.success();
    }

    @ApiErrorResponses({BaseResponseStatus.NO_EXIST_SUBMISSION})
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/submission/{submissionId}/download")
    public ResponseEntity<FileSystemResource> download(@PathVariable("submissionId") Long submissionId) {
        FileSystemResource resource = adminService.downloadSubmissionZip(submissionId);
        File file = resource.getFile();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
