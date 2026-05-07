package com.hackplay.hackplay.service;

import com.hackplay.hackplay.common.enums.project.NodeType;
import com.hackplay.hackplay.dto.*;

import java.io.IOException;

public interface NodeService {
    void create(String uuid, Long projectId, NodeCreateReqDto dto) throws IOException;
    FileRespDto getFileContent(Long projectId, String path) throws IOException;
    void updateFileContent(Long projectId, FileUpdateReqDto dto) throws IOException;
    DirectoryTreeRespDto getTree(Long projectId);
    void rename(Long projectId, NodeRenameReqDto dto) throws IOException;
    void move(Long projectId, NodeMoveReqDto dto) throws IOException;
    void delete(Long projectId, String path, NodeType type) throws IOException;
}
