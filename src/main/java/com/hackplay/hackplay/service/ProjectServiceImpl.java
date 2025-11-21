package com.hackplay.hackplay.service;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
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
import com.hackplay.hackplay.domain.Submission;
import com.hackplay.hackplay.dto.ProjectCreateReqDto;
import com.hackplay.hackplay.dto.ProjectRespDto;
import com.hackplay.hackplay.dto.ProjectUpdateReqDto;
import com.hackplay.hackplay.repository.MemberProgressRepository;
import com.hackplay.hackplay.repository.MemberRepository;
import com.hackplay.hackplay.repository.ProjectRepository;

import lombok.RequiredArgsConstructor;

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
    public void create(ProjectCreateReqDto projectCreateReqDto) throws IOException, InterruptedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String uuid = (String) authentication.getPrincipal();

        Member member = memberRepository.findByUuid(uuid)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS));

        Project project = Project.builder()
                .name(projectCreateReqDto.getName())
                .description(projectCreateReqDto.getDescription())
                .templateType(projectCreateReqDto.getTemplateType())
                .isPublic(projectCreateReqDto.getIsPublic())
                .lecture(projectCreateReqDto.getLecture())
                .member(member)
                .build();

        projectRepository.save(project);

        Path projectPath = Paths.get(projectsBasePath, project.getUuid().toString()).toAbsolutePath();

        String scriptPath = scriptsBasePath + "/create-" + projectCreateReqDto.getTemplateType() + ".sh";

        if (!Files.exists(Paths.get(scriptPath))) {
            throw new BaseException(BaseResponseStatus.SCRIPT_NOT_FOUND);
        }

        ProcessBuilder pb = new ProcessBuilder(
            "sh", scriptPath,
            projectPath.toString(),
            project.getName()
        );
        pb.inheritIO().start().waitFor();

        MemberProgress progress = MemberProgress.builder()
                .member(member)
                .project(project)
                .build();

        memberProgressRepository.save(progress);
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

        Member member = memberRepository.findByUuid(uuid).orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS));

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

        project.updateProjectInfo(projectUpdateReqDto.getName(), projectUpdateReqDto.getDescription(), projectUpdateReqDto.getIsPublic());
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
