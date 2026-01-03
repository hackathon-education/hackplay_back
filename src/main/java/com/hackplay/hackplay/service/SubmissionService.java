package com.hackplay.hackplay.service;

import java.io.IOException;
import java.util.List;

import com.hackplay.hackplay.dto.SubmissionDetailRespDto;
import com.hackplay.hackplay.dto.SubmissionListRespDto;
import com.hackplay.hackplay.dto.SubmissionReqDto;

public interface SubmissionService {
    void submit(String uuid, SubmissionReqDto submissionReqDto) throws IOException;
    List<SubmissionListRespDto> getMySubmissions(String uuid);
    SubmissionDetailRespDto getSubmissionDetail(String uuid, Long submissionId);
}