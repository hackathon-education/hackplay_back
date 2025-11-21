package com.hackplay.hackplay.service;

import java.util.List;

import org.springframework.core.io.FileSystemResource;

import com.hackplay.hackplay.common.CommonEnums.SubmissionStatus;
import com.hackplay.hackplay.dto.AdminSubmissionDetailRespDto;
import com.hackplay.hackplay.dto.AdminSubmissionListRespDto;

public interface AdminService {
    List<AdminSubmissionListRespDto> getAllSubmissions();
    AdminSubmissionDetailRespDto getSubmissionDetail(Long submissionId);
    void grade(Long submissionId, SubmissionStatus status);
    FileSystemResource downloadSubmissionZip(Long submissionId);
}