package com.hackplay.hackplay.service;

import java.io.File;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackplay.hackplay.common.BaseException;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.common.enums.submission.SubmissionStatus;
import com.hackplay.hackplay.domain.Project;
import com.hackplay.hackplay.dto.AdminSubmissionDetailRespDto;
import com.hackplay.hackplay.dto.AdminSubmissionListRespDto;
import com.hackplay.hackplay.repository.ProjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final ProjectRepository projectRepository;

    // 전체 제출 목록 조회
    @Override
    public List<AdminSubmissionListRespDto> getAllSubmissions() {

        List<Project> submissions = projectRepository.findByStatus(SubmissionStatus.PENDING);

        return submissions.stream()
                .map(AdminSubmissionListRespDto::from)
                .toList();
    }

    // 제출 상세 조회
    @Override
    public AdminSubmissionDetailRespDto getSubmissionDetail(Long submissionId) {

        Project project = projectRepository.findById(submissionId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_SUBMISSION));

        return AdminSubmissionDetailRespDto.from(project);
    }

    // 채점 (PASS / FAIL)
    @Override
    @Transactional
    public void grade(Long submissionId, SubmissionStatus status) {

        Project project = projectRepository.findById(submissionId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_SUBMISSION));

        project.updateSubmissionStatus(status);

        if (status == SubmissionStatus.PASS) {
            updateProgressOnPass(project);
        }
    }

    // PASS 시 다음 주차 unlock (강의는 N주차까지 있음 -> N주차 강의까지 PASS시 강의 완료)
    @Transactional
    private void updateProgressOnPass(Project project) {

        int currentWeek = project.getCurrentWeek();
        int totalWeeks = project.getTotalWeek();

        // 마지막 주차라면 완료 처리
        if (currentWeek == totalWeeks) {
            project.complete();
            return;
        }

        // 다음 주차로 이동
        project.unlockNextWeek();
    }

    @Override
    public FileSystemResource downloadSubmissionZip(Long submissionId) {

        Project project = projectRepository.findById(submissionId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_SUBMISSION));

        File file = new File(project.getZipPath());
        if (!file.exists()) {
            throw new BaseException(BaseResponseStatus.FILE_NOT_FOUND);
        }

        return new FileSystemResource(file);
    }

}
