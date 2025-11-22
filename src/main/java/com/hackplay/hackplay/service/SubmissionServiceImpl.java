package com.hackplay.hackplay.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackplay.hackplay.common.BaseException;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.common.ZipUtils;
import com.hackplay.hackplay.domain.Member;
import com.hackplay.hackplay.domain.MemberProgress;
import com.hackplay.hackplay.domain.Project;
import com.hackplay.hackplay.domain.Submission;
import com.hackplay.hackplay.dto.SubmissionDetailRespDto;
import com.hackplay.hackplay.dto.SubmissionListRespDto;
import com.hackplay.hackplay.dto.SubmissionReqDto;
import com.hackplay.hackplay.repository.MemberProgressRepository;
import com.hackplay.hackplay.repository.MemberRepository;
import com.hackplay.hackplay.repository.ProjectRepository;
import com.hackplay.hackplay.repository.SubmissionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubmissionServiceImpl implements SubmissionService {

        private final SubmissionRepository submissionRepository;
        private final ProjectRepository projectRepository;
        private final MemberRepository memberRepository;
        private final MemberProgressRepository memberProgressRepository;

        @Override
        @Transactional
        public void submit(SubmissionReqDto submissionReqDto) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String uuid = (String) authentication.getPrincipal();

        Member member = memberRepository.findByUuid(uuid)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS));

        Project project = projectRepository.findById(submissionReqDto.getProjectId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND));

        // 진행중인 주차 확인
        MemberProgress progress = memberProgressRepository.findByMemberAndProject(member, project)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_PROGRESS));

        int week = progress.getCurrentWeek();

        // 실제 프로젝트 폴더 경로
        Path sourceDir = Paths.get("../projects/" + project.getUuid());

        // 디버깅 (현재 작업 디렉토리)
        // System.out.println(">> CWD: " + new File(".").getAbsolutePath());
        // System.out.println(">> SourceDir: " + sourceDir.toAbsolutePath());
        // System.out.println(">> Exists? " + Files.exists(sourceDir));

        if (!Files.exists(sourceDir)) {
                throw new BaseException(BaseResponseStatus.WORKSPACE_NOT_FOUND);
        }

        // ZIP 파일 저장 폴더
        String zipDir = "../submissions/" + project.getUuid() + "/" + week + "/";
        Files.createDirectories(Paths.get(zipDir));

        // 디버깅
        // System.out.println(">> ZipDir: " + Paths.get(zipDir).toAbsolutePath());

        // zip 파일 이름
        String zipName = uuid + "_" + System.currentTimeMillis() + ".zip";
        Path zipPath = Paths.get(zipDir + zipName);

        // zip 생성
        ZipUtils.zipDirectory(sourceDir, zipPath);

        // DB 저장
        Submission submission = Submission.builder()
                .member(member)
                .project(project)
                .week(week)
                .zipPath(zipPath.toString())
                .build();

        submissionRepository.save(submission);
        }

        // 유저 - 내 제출 목록 조회
        @Override
        public List<SubmissionListRespDto> getMySubmissions() {
                Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
                String uuid = (String)authentication.getPrincipal();

                Member member = memberRepository
                .findByUuid(uuid)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS));

                List<Submission> submissions = submissionRepository.findAllByMember(member);

                return submissions
                .stream()
                .map(SubmissionListRespDto::from)
                .toList();
        }

        // 유저 - 제출 상세 조회
        @Override
        public SubmissionDetailRespDto getSubmissionDetail(Long submissionId) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String uuid = (String)authentication.getPrincipal();

                Member member = memberRepository.findByUuid(uuid).orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS));

                Submission submission = submissionRepository.findById(submissionId).orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_SUBMISSION));

                // 본인 제출이 아닌 경우 차단
                if (!submission.getMember().getId().equals(member.getId())) {
                throw new BaseException(BaseResponseStatus.NO_PERMISSION);
                }

                return SubmissionDetailRespDto.from(submission);
        }
}
