package com.hackplay.hackplay.service;

import java.io.IOException;

import com.hackplay.hackplay.dto.FileCreateReqDto;
import com.hackplay.hackplay.dto.FileRespDto;
import com.hackplay.hackplay.dto.FileUpdateReqDto;

public interface FileService {
    void create(Long projectId, FileCreateReqDto fileCreateReqDto) throws IOException;
    FileRespDto getFile(Long projectId, String path) throws IOException;
    void update(Long projectId, FileUpdateReqDto fileUpdateReqDto) throws IOException;
    void delete(Long projectId, String path) throws IOException;
}