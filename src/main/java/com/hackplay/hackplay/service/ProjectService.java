package com.hackplay.hackplay.service;

import java.io.IOException;
import java.util.List;

import com.hackplay.hackplay.common.CommonEnums;
import com.hackplay.hackplay.dto.LectureProgressRespDto;
import com.hackplay.hackplay.dto.ProjectCreateReqDto;
import com.hackplay.hackplay.dto.ProjectRespDto;
import com.hackplay.hackplay.dto.ProjectUpdateReqDto;

public interface ProjectService {
    void create(String memberUuid, ProjectCreateReqDto request) throws IOException, InterruptedException;
    LectureProgressRespDto getLectureProgress(String uuid, CommonEnums.Lecture lecture);
    List<ProjectRespDto> getProjects(String uuid);
    ProjectRespDto getProject(String uuid, Long projectId);
    void update(Long projectId, ProjectUpdateReqDto request);
    void delete(Long projectId);
}
