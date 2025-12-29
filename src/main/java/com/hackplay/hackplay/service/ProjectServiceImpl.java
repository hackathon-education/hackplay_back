package com.hackplay.hackplay.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackplay.hackplay.common.BaseException;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.domain.Member;
import com.hackplay.hackplay.domain.MemberProgress;
import com.hackplay.hackplay.domain.Project;
import com.hackplay.hackplay.dto.ProjectCreateReqDto;
import com.hackplay.hackplay.dto.ProjectRespDto;
import com.hackplay.hackplay.dto.ProjectUpdateReqDto;
import com.hackplay.hackplay.repository.MemberProgressRepository;
import com.hackplay.hackplay.repository.MemberRepository;
import com.hackplay.hackplay.repository.ProjectRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final MemberProgressRepository memberProgressRepository;

    @Value("${projects.base-path}")
    private String projectsBasePath;

    @Value("${scripts.base-path}")
    private String scriptsBasePath;

    @Override
    @Transactional
    public void create(ProjectCreateReqDto projectCreateReqDto)
            throws IOException, InterruptedException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String memberUuid = (String) authentication.getPrincipal();

        Member member = memberRepository.findByUuid(memberUuid)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS));

        UUID projectUuid = UUID.randomUUID();

        // 호스트 경로 (파일 존재 확인 및 DB 저장용)
        Path projectPath = Paths
                .get(projectsBasePath, projectUuid.toString())
                .toAbsolutePath();

        // 컨테이너 내부 경로 사용
        String containerScriptPath = "/scripts/create-" + projectCreateReqDto.getTemplateType() + ".sh";
        String containerProjectPath = "/projects/" + projectUuid.toString();

        // 호스트에서 스크립트 파일 존재 확인 (선택사항)
        String hostScriptPath = Paths.get(
                scriptsBasePath,
                "create-" + projectCreateReqDto.getTemplateType() + ".sh"
        ).toAbsolutePath().toString();

        if (!Files.exists(Paths.get(hostScriptPath))) {
            throw new BaseException(BaseResponseStatus.SCRIPT_NOT_FOUND);
        }

        // Docker 명령어 실행을 위한 ProcessBuilder 설정 (컨테이너 내부 경로 사용)
        ProcessBuilder pb = new ProcessBuilder(
                "/usr/bin/docker", "exec",
                "-i",
                "-w", "/",
                "hackplay-generator",
                "/bin/bash",
                containerScriptPath,
                containerProjectPath,
                projectCreateReqDto.getName()
        );

        // 환경 변수 설정
        Map<String, String> env = pb.environment();
        env.put("DOCKER_HOST", "unix:///var/run/docker.sock");
        env.put("PATH", "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin");

        pb.redirectErrorStream(true);

        log.info("Starting project creation for: {}", projectCreateReqDto.getName());
        log.info("Project UUID: {}", projectUuid);
        log.info("Host project path: {}", projectPath);
        log.info("Host script path: {}", hostScriptPath);
        log.info("Container script path: {}", containerScriptPath);
        log.info("Container project path: {}", containerProjectPath);
        log.info("Command: {}", String.join(" ", pb.command()));

        Process process = pb.start();

        // 프로세스 출력 로깅
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                log.info("[GENERATOR] {}", line);
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        log.info("Process exit code: {}", exitCode);

        if (exitCode != 0) {
            log.error("Project creation script failed with exit code: {}", exitCode);
            log.error("Script output: {}", output.toString());
            throw new BaseException(BaseResponseStatus.PROJECT_CREATION_FAILED);
        }

        // 프로젝트 폴더가 실제로 생성되었는지 확인
        if (!Files.exists(projectPath)) {
            log.error("Project directory was not created: {}", projectPath);
            throw new BaseException(BaseResponseStatus.PROJECT_CREATION_FAILED);
        }

        // 프로젝트 폴더에 기본 파일들이 있는지 확인 (추가 검증)
        try {
            long fileCount = Files.list(projectPath).count();
            if (fileCount == 0) {
                log.error("Project directory is empty: {}", projectPath);
                throw new BaseException(BaseResponseStatus.PROJECT_CREATION_FAILED);
            }
            log.info("Project created successfully with {} files/directories", fileCount);
        } catch (IOException e) {
            log.error("Error checking project directory contents: {}", e.getMessage());
            throw new BaseException(BaseResponseStatus.PROJECT_CREATION_FAILED);
        }

        // 데이터베이스에 프로젝트 저장
        Project project = Project.builder()
                .uuid(projectUuid)
                .name(projectCreateReqDto.getName())
                .description(projectCreateReqDto.getDescription())
                .templateType(projectCreateReqDto.getTemplateType())
                .isPublic(projectCreateReqDto.getIsPublic())
                .lecture(projectCreateReqDto.getLecture())
                .member(member)
                .build();

        projectRepository.save(project);

        memberProgressRepository.save(
                MemberProgress.builder()
                        .member(member)
                        .project(project)
                        .build()
        );

        log.info("Project created and saved to database successfully: {}", projectCreateReqDto.getName());
    }

    @Override
    public List<ProjectRespDto> getProjects() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String uuid = (String) authentication.getPrincipal();

        Member member = memberRepository.findByUuid(uuid)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS));

        return projectRepository.findAll().stream()
                .map(project -> {
                    Integer week = memberProgressRepository
                            .findByMemberAndProject(member, project)
                            .map(MemberProgress::getCurrentWeek)
                            .orElse(1);

                    return ProjectRespDto.from(project, week);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ProjectRespDto getProject(Long projectId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String uuid = (String)authentication.getPrincipal();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND));

        Member member = memberRepository.findByUuid(uuid)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS));

        MemberProgress progress = memberProgressRepository.findByMemberAndProject(
                member,
                project
        )
        .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_PROGRESS));

        return ProjectRespDto.from(project, progress.getCurrentWeek());
    }

    @Override
    @Transactional
    public void update(Long projectId, ProjectUpdateReqDto projectUpdateReqDto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND));

        project.updateProjectInfo(
                projectUpdateReqDto.getName(),
                projectUpdateReqDto.getDescription(),
                projectUpdateReqDto.getIsPublic()
        );
    }

    @Override
    @Transactional
    public void delete(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND));

        Path projectPath = Paths.get(projectsBasePath, project.getUuid().toString());
        if (Files.exists(projectPath)) {
            try {
                Files.walk(projectPath)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException ignored) {}
                        });
            } catch (IOException e) {
                throw new BaseException(BaseResponseStatus.INTERNAL_SERVER_ERROR);
            }
        }

        projectRepository.delete(project);
    }
}