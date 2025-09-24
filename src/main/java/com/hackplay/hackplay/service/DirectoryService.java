package com.hackplay.hackplay.service;

import java.io.IOException;

import com.hackplay.hackplay.dto.DirectoryCreateReqDto;
import com.hackplay.hackplay.dto.DirectoryTreeRespDto;
import com.hackplay.hackplay.dto.DirectoryUpdateReqDto;

public interface DirectoryService {
    // 디렉토리 생성
    void create(Long projectId, DirectoryCreateReqDto directoryCreateReqDto) throws IOException;
    // 하위 디렉토리 조회
    DirectoryTreeRespDto view(Long projectId);
    // 디렉토리명 수정
    void update(Long projectId, DirectoryUpdateReqDto directoryUpdateReqDto) throws IOException;
    // 디렉토리 삭제 (하위 디렉토리/파일 포함)
    void delete(Long projectId, String dirPath) throws IOException;
}
