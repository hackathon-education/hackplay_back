package com.hackplay.hackplay.service;

import com.hackplay.hackplay.common.BaseException;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.domain.Project;
import com.hackplay.hackplay.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class ProjectWorkspaceService {

    private final ProjectRepository projectRepository;

    // Hackplay 루트에서 ./projects 를 마운트하고 있다고 가정
    private final Path projectsRoot = Paths.get("../projects").toAbsolutePath().normalize();

    public Path resolveProjectRoot(long projectId, String uuid) {

    Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND));

        // ✅ 프로젝트 소유자/권한 검증 (치명 중요)
        if (!project.isAccessibleBy(uuid)) {
            throw new BaseException(BaseResponseStatus.NO_PERMISSION);
        }

        Path projectRoot = projectsRoot
                .resolve(project.getUuid())
                .normalize();

        // ✅ Path Traversal 방어
        if (!projectRoot.startsWith(projectsRoot)) {
            throw new SecurityException("Invalid project root access");
        }

        return projectRoot;
    }
}
