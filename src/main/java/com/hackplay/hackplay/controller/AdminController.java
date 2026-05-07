package com.hackplay.hackplay.controller;

import java.io.File;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.hackplay.hackplay.common.ApiResponse;
import com.hackplay.hackplay.dto.AdminGradeReqDto;
import com.hackplay.hackplay.dto.AdminSubmissionDetailRespDto;
import com.hackplay.hackplay.dto.AdminSubmissionListRespDto;
import com.hackplay.hackplay.service.AdminService;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<List<AdminSubmissionListRespDto>> getAllSubmissions() {
        return ApiResponse.success(adminService.getAllSubmissions());
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "제출 내역이 존재하지 않습니다.")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{submissionId}")
    public ApiResponse<AdminSubmissionDetailRespDto> getSubmissionDetail(@PathVariable("submissionId") Long submissionId) {
        return ApiResponse.success(adminService.getSubmissionDetail(submissionId));
    }

    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "제출 내역이 존재하지 않습니다.")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{submissionId}/grade")
    public ApiResponse<String> gradeSubmission(
            @PathVariable("submissionId") Long submissionId,
            @Valid @RequestBody AdminGradeReqDto adminGradeReqDto) {
        adminService.grade(submissionId, adminGradeReqDto.getStatus());
        return ApiResponse.success();
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "제출 내역이 존재하지 않습니다.")
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
