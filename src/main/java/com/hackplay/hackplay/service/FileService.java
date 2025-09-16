package com.hackplay.hackplay.service;

import java.io.IOException;

import com.hackplay.hackplay.dto.FileCreateReqDto;
import com.hackplay.hackplay.dto.FileRespDto;
import com.hackplay.hackplay.dto.FileUpdateReqDto;

public interface FileService {
    void create(Long projectId, FileCreateReqDto dto) throws IOException;
    FileRespDto getFile(Long projectId, Long fileId) throws IOException;
    void update(Long projectId, Long fileId, FileUpdateReqDto dto) throws IOException;
    void delete(Long projectId, Long fileId) throws IOException;
}
