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
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackplay.hackplay.common.BaseException;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.common.CommonEnums;
import com.hackplay.hackplay.common.CommonEnums.WeekProgressStatus;
import com.hackplay.hackplay.domain.Member;
import com.hackplay.hackplay.domain.MemberProgress;
import com.hackplay.hackplay.domain.Project;
import com.hackplay.hackplay.dto.LectureProgressRespDto;
import com.hackplay.hackplay.dto.LectureWeekProgressDto;
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
    public void create(String memberUuid, ProjectCreateReqDto projectCreateReqDto)
            throws IOException, InterruptedException {

        Member member = memberRepository.findByUuid(memberUuid)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS));

        // ===============================
        // 강의별 프로젝트 1개 제한
        // ===============================
        if (projectRepository.existsByMemberAndLecture(member, projectCreateReqDto.getLecture())) {
            throw new BaseException(BaseResponseStatus.PROJECT_ALREADY_EXISTS_FOR_LECTURE);
        }

        // ===============================
        // Project 생성
        // ===============================
        UUID projectUuid = UUID.randomUUID();

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

        // ===============================
        // 경로 설정
        // ===============================
        Path projectPath = Paths.get(projectsBasePath, projectUuid.toString()).toAbsolutePath();
        Path scriptPath = Paths.get(
                scriptsBasePath,
                "create-" + projectCreateReqDto.getTemplateType() + ".sh"
        ).toAbsolutePath();

        if (!Files.exists(scriptPath)) {
            throw new BaseException(BaseResponseStatus.SCRIPT_NOT_FOUND);
        }

        log.info("Project UUID: {}", projectUuid);
        log.info("Project Path: {}", projectPath);
        log.info("Script Path: {}", scriptPath);

        // ===============================
        // ProcessBuilder 생성
        // ===============================
        ProcessBuilder pb = buildProcess(
                scriptPath,
                projectPath,
                project.getName(),
                projectCreateReqDto.getTemplateType()
        );

        pb.redirectErrorStream(true);
        log.info("Command: {}", String.join(" ", pb.command()));

        // ===============================
        // 실행
        // ===============================
        Process process = pb.start();

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
        log.info("Script exit code: {}", exitCode);

        if (exitCode != 0) {
            log.error("Project creation failed\n{}", output);
            throw new BaseException(BaseResponseStatus.PROJECT_CREATION_FAILED);
        }

        // ===============================
        // 생성 검증
        // ===============================
        if (!Files.exists(projectPath) || Files.list(projectPath).findAny().isEmpty()) {
            throw new BaseException(BaseResponseStatus.PROJECT_CREATION_FAILED);
        }

        log.info("Project created successfully");
    }

    private ProcessBuilder buildProcess(
            Path scriptPath,
            Path projectPath,
            String projectName,
            String templateType
    ) {
        return isWindows()
                ? buildWindowsProcess(scriptPath, projectPath, projectName)
                : buildLinuxProcess(projectPath, projectName, templateType);
    }

    private ProcessBuilder buildWindowsProcess(
            Path scriptPath,
            Path projectPath,
            String projectName
    ) {
        String bashExe = "C:/Program Files/Git/bin/bash.exe";

        return new ProcessBuilder(
                bashExe,
                toBashPath(scriptPath),
                toBashPath(projectPath),
                projectName
        );
    }

    private ProcessBuilder buildLinuxProcess(
            Path projectPath,
            String projectName,
            String templateType
    ) {
        ProcessBuilder pb = new ProcessBuilder(
                "/usr/bin/docker", "exec",
                "-i",
                "-w", "/",
                "hackplay-generator",
                "/bin/bash",
                "/scripts/create-" + templateType + ".sh",
                "/projects/" + projectPath.getFileName(),
                projectName
        );

        Map<String, String> env = pb.environment();
        env.put("DOCKER_HOST", "unix:///var/run/docker.sock");
        env.put("PATH", "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin");

        return pb;
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private String toBashPath(Path path) {
        return path.toAbsolutePath()
                .toString()
                .replace("\\", "/")
                .replaceFirst("^([A-Za-z]):", "/$1")
                .toLowerCase();
    }

    @Override
    public LectureProgressRespDto getLectureProgress(String uuid, CommonEnums.Lecture lecture) {

        Member member = memberRepository.findByUuid(uuid)
            .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS));

        Project project = projectRepository
            .findByMemberAndLecture(member, lecture)
            .orElse(null);

        // 프로젝트가 아직 없으면 → 전부 NOT_STARTED
        if (project == null) {
            List<LectureWeekProgressDto> weeks =
                IntStream.rangeClosed(1, lecture.getTotalWeek())
                    .mapToObj(w -> new LectureWeekProgressDto(
                        w,
                        WeekProgressStatus.NOT_STARTED,
                        null
                    ))
                    .toList();

            return new LectureProgressRespDto(weeks);
        }

        MemberProgress progress = memberProgressRepository
            .findByMemberAndProject(member, project)
            .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_PROGRESS));

        int currentWeek = progress.getCurrentWeek();
        boolean isCompleted = progress.isCompleted();

        List<LectureWeekProgressDto> weeks =
            IntStream.rangeClosed(1, project.getTotalWeek())
                .mapToObj(week -> {

                    if (isCompleted || week < currentWeek) {
                        return new LectureWeekProgressDto(
                            week,
                            WeekProgressStatus.COMPLETED,
                            project.getId()
                        );
                    }

                    if (week == currentWeek) {
                        return new LectureWeekProgressDto(
                            week,
                            WeekProgressStatus.IN_PROGRESS,
                            project.getId()
                        );
                    }

                    return new LectureWeekProgressDto(
                        week,
                        WeekProgressStatus.NOT_STARTED,
                        null
                    );
                })
                .toList();

        return new LectureProgressRespDto(weeks);
    }


    @Override
    public List<ProjectRespDto> getProjects(String uuid) {
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
    public ProjectRespDto getProject(String uuid, Long projectId) {

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

        memberProgressRepository.deleteByProject(project);

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