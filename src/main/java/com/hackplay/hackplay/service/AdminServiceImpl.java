package com.hackplay.hackplay.service;

import java.io.File;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackplay.hackplay.common.BaseException;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.common.CommonEnums;
import com.hackplay.hackplay.common.CommonEnums.SubmissionStatus;
import com.hackplay.hackplay.domain.MemberProgress;
import com.hackplay.hackplay.domain.Submission;
import com.hackplay.hackplay.dto.AdminSubmissionDetailRespDto;
import com.hackplay.hackplay.dto.AdminSubmissionListRespDto;
import com.hackplay.hackplay.repository.MemberProgressRepository;
import com.hackplay.hackplay.repository.SubmissionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final SubmissionRepository submissionRepository;
    private final MemberProgressRepository memberProgressRepository;

    // 전체 제출 목록 조회
    @Override
    public List<AdminSubmissionListRespDto> getAllSubmissions() {

        List<Submission> submissions = submissionRepository.findAll();

        return submissions.stream()
                .map(AdminSubmissionListRespDto::from)
                .toList();
    }

    // 제출 상세 조회
    @Override
    public AdminSubmissionDetailRespDto getSubmissionDetail(Long submissionId) {

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_SUBMISSION));

        return AdminSubmissionDetailRespDto.from(submission);
    }

    // 채점 (PASS / FAIL)
    @Override
    @Transactional
    public void grade(Long submissionId, CommonEnums.SubmissionStatus status) {

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_SUBMISSION));

        submission.updateStatus(status);

        if (status == SubmissionStatus.PASS) {
            updateProgressOnPass(submission);
        }
    }

    // PASS 시 다음 주차 unlock (강의는 N주차까지 있음 -> N주차 강의까지 PASS시 강의 완료)
    @Transactional
    private void updateProgressOnPass(Submission submission) {

        MemberProgress memberProgress = memberProgressRepository
                .findByMemberAndProject(submission.getMember(), submission.getProject())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_PROGRESS));

        int currentWeek = memberProgress.getCurrentWeek();
        int totalWeeks = submission.getProject().getTotalWeek();

        // 마지막 주차라면 완료 처리
        if (currentWeek == totalWeeks) {
            memberProgress.complete();
            return;
        }

        // 다음 주차로 이동
        memberProgress.unlockNextWeek();
    }

    @Override
    public FileSystemResource downloadSubmissionZip(Long submissionId) {

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_SUBMISSION));

        File file = new File(submission.getZipPath());
        if (!file.exists()) {
            throw new BaseException(BaseResponseStatus.FILE_NOT_FOUND);
        }

        return new FileSystemResource(file);
    }

}
