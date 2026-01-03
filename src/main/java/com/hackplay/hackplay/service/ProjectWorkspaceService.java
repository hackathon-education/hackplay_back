package com.hackplay.hackplay.service;

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

    public Path resolveProjectRoot(long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        Path root = projectsRoot.resolve(project.getUuid()).normalize();
        if (!root.startsWith(projectsRoot)) {
            throw new SecurityException("Invalid project root");
        }
        return root;
    }
}
