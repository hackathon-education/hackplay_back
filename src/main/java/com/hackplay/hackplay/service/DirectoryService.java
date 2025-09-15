package com.hackplay.hackplay.service;

import com.hackplay.hackplay.dto.DirectoryCreateReqDto;
import com.hackplay.hackplay.dto.DirectoryTreeRespDto;
import com.hackplay.hackplay.dto.DirectoryUpdateReqDto;

public interface DirectoryService {
    // 디렉토리 생성
    void create(Long projectId, DirectoryCreateReqDto directoryCreateReqDto);
    // 하위 디렉토리 조회
    DirectoryTreeRespDto view(Long projectId, Long dirId);
    // 디렉토리명 수정
    void update(Long projectId, Long dirId, DirectoryUpdateReqDto directoryUpdateReqDto);
    // 디렉토리 삭제 (하위 디렉토리/파일 포함)
    void delete(Long projectId, Long dirId);
}
