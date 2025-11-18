package com.hackplay.hackplay.service;

import com.hackplay.hackplay.dto.FileCreateReqDto;
import com.hackplay.hackplay.dto.FileMoveReqDto;
import com.hackplay.hackplay.dto.FileRenameReqDto;
import com.hackplay.hackplay.dto.FileRespDto;
import com.hackplay.hackplay.dto.FileUpdateReqDto;

import java.io.IOException;

public interface FileService {
    void create(Long projectId, FileCreateReqDto fileCreateReqDto) throws IOException;
    
    FileRespDto getFile(Long projectId, String path) throws IOException;
    
    void update(Long projectId, FileUpdateReqDto fileUpdateReqDto) throws IOException;

    void rename(Long projectId, FileRenameReqDto fileRenameReqDto) throws IOException;

    void move(Long projectId, FileMoveReqDto fileMoveReqDto) throws IOException;
    
    void delete(Long projectId, String path) throws IOException;
    
    /**
     * 프로젝트 ID로 프로젝트 UUID를 반환
     * @param projectId 프로젝트 ID
     * @return 프로젝트 UUID
     */
    default String getProjectUuid(Long projectId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}