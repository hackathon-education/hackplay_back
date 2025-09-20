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
import com.hackplay.hackplay.domain.Project;
import com.hackplay.hackplay.dto.ProjectCreateReqDto;
import com.hackplay.hackplay.dto.ProjectRespDto;
import com.hackplay.hackplay.dto.ProjectUpdateReqDto;
import com.hackplay.hackplay.repository.MemberRepository;
import com.hackplay.hackplay.repository.ProjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;

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
    }


    @Override
    public List<ProjectRespDto> getProjects() {
        return projectRepository.findAll().stream()
                .map(ProjectRespDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectRespDto getProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND));

        return ProjectRespDto.from(project);
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

        stop(projectId);

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

    private ProcessBuilder createNpmProcess(Path projectPath) {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return new ProcessBuilder(
                "cmd.exe", "/c", "npm", "run", "dev"
            );
        } else {
            return new ProcessBuilder("npm", "run", "dev");
        }
    }

    @Override
    @Transactional
    public void start(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND));

        Path projectPath = Paths.get(projectsBasePath, project.getUuid().toString());
        
        try {
            ProcessBuilder pb = createNpmProcess(projectPath);
            pb.directory(projectPath.toFile());
            pb.start();
            
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void stop(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND));

        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                ProcessBuilder pb = new ProcessBuilder(
                    "cmd.exe", "/c", 
                    "for /f \"tokens=5\" %a in ('netstat -aon ^| findstr :3000') do taskkill /f /pid %a"
                );
                Process process = pb.start();
                process.waitFor();
            } else {
                ProcessBuilder pb = new ProcessBuilder(
                    "pkill", "-f", "vite.*" + project.getUuid().toString()
                );
                Process process = pb.start();
                process.waitFor();
            }
            
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
