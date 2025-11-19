package com.hackplay.hackplay.service;

import java.io.IOException;
import java.util.List;

import com.hackplay.hackplay.dto.ProjectCreateReqDto;
import com.hackplay.hackplay.dto.ProjectRespDto;
import com.hackplay.hackplay.dto.ProjectUpdateReqDto;

public interface ProjectService {
    void create(ProjectCreateReqDto request) throws IOException, InterruptedException;
    List<ProjectRespDto> getProjects();
    ProjectRespDto getProject(Long projectId);
    void update(Long projectId, ProjectUpdateReqDto request);
    void delete(Long projectId);
}
