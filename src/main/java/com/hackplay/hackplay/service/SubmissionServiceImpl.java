package com.hackplay.hackplay.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackplay.hackplay.common.BaseException;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.common.ZipUtils;
import com.hackplay.hackplay.common.enums.submission.SubmissionStatus;
import com.hackplay.hackplay.domain.Member;
import com.hackplay.hackplay.domain.Project;
import com.hackplay.hackplay.dto.SubmissionDetailRespDto;
import com.hackplay.hackplay.dto.SubmissionListRespDto;
import com.hackplay.hackplay.dto.SubmissionReqDto;
import com.hackplay.hackplay.repository.MemberRepository;
import com.hackplay.hackplay.repository.ProjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubmissionServiceImpl implements SubmissionService {

        private final ProjectRepository projectRepository;
        private final MemberRepository memberRepository;

        // 유저 - 제출
        @Override
        @Transactional
        public void submit(String uuid, SubmissionReqDto submissionReqDto) throws IOException {

                Project project = projectRepository.findById(submissionReqDto.getProjectId())
                        .orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND));

                int week = project.getCurrentWeek();

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

                // 제출 정보 업데이트
                project.updateSubmission(zipPath.toString(), SubmissionStatus.PENDING);
        }

        // 유저 - 내 제출 목록 조회
        @Override
        public List<SubmissionListRespDto> getMySubmissions(String uuid) {
                Member member = memberRepository
                .findByUuid(uuid)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS));

                List<Project> projects = projectRepository.findByMember(member);

                return projects.stream()
                        .map(SubmissionListRespDto::from)
                        .toList();
        }

        // 유저 - 제출 상세 조회
        @Override
        public SubmissionDetailRespDto getSubmissionDetail(String uuid, Long projectId) {

                Member member = memberRepository.findByUuid(uuid).orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS));

                Project project = projectRepository.findById(projectId).orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_SUBMISSION));

                // 본인 제출이 아닌 경우 차단
                if (!project.getMember().getId().equals(member.getId())) {
                throw new BaseException(BaseResponseStatus.NO_PERMISSION);
                }

                return SubmissionDetailRespDto.from(project);
        }
}
