package com.hackplay.hackplay.controller;

import java.io.File;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackplay.hackplay.common.ApiResponse;
import com.hackplay.hackplay.dto.AdminGradeReqDto;
import com.hackplay.hackplay.dto.AdminSubmissionDetailRespDto;
import com.hackplay.hackplay.dto.AdminSubmissionListRespDto;
import com.hackplay.hackplay.service.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    // 제출 전체 조회
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<List<AdminSubmissionListRespDto>> getAllSubmissions() {
        return ApiResponse.success(adminService.getAllSubmissions());
    }

    // 제출 상세 조회
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{submissionId}")
    public ApiResponse<AdminSubmissionDetailRespDto> getSubmissionDetail(@PathVariable("submissionId") Long submissionId) {
        return ApiResponse.success(adminService.getSubmissionDetail(submissionId));
    }

    // 제출 채점 (PASS / FAIL)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{submissionId}/grade")
    public ApiResponse<String> gradeSubmission(
            @PathVariable("submissionId") Long submissionId,
            @Valid @RequestBody AdminGradeReqDto adminGradeReqDto) {

        adminService.grade(submissionId, adminGradeReqDto.getStatus());
        return ApiResponse.success();
    }

    // Zip 다운로드
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