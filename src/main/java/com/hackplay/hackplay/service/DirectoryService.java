package com.hackplay.hackplay.service;

import java.io.IOException;

import com.hackplay.hackplay.dto.DirectoryCreateReqDto;
import com.hackplay.hackplay.dto.DirectoryMoveReqDto;
import com.hackplay.hackplay.dto.DirectoryRenameReqDto;
import com.hackplay.hackplay.dto.DirectoryTreeRespDto;

public interface DirectoryService {
    // 디렉토리 생성
    void create(Long projectId, DirectoryCreateReqDto directoryCreateReqDto) throws IOException;
    // 하위 디렉토리 조회
    DirectoryTreeRespDto view(Long projectId);
    // 디렉토리 이름 변경
    void rename(Long projectId, DirectoryRenameReqDto directoryRenameReqDto) throws IOException;
    // 디렉토리 위치 이동
    void move(Long projectId, DirectoryMoveReqDto directoryMoveReqDto) throws IOException;
    // 디렉토리 삭제 (하위 디렉토리/파일 포함)
    void delete(Long projectId, String dirPath) throws IOException;
}
