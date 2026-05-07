package com.hackplay.hackplay.service;

import com.hackplay.hackplay.common.enums.project.NodeType;
import com.hackplay.hackplay.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class NodeServiceImpl implements NodeService {

    private final FileService fileService;
    private final DirectoryService directoryService;

    @Override
    public void create(String uuid, Long projectId, NodeCreateReqDto dto) throws IOException {
        if (dto.getType() == NodeType.FILE) {
            FileCreateReqDto req = new FileCreateReqDto();
            req.setName(dto.getName());
            req.setParentPath(dto.getParentPath());
            req.setContent(dto.getContent() != null ? dto.getContent() : "");
            fileService.create(uuid, projectId, req);
        } else {
            DirectoryCreateReqDto req = new DirectoryCreateReqDto();
            req.setName(dto.getName());
            req.setParentPath(dto.getParentPath());
            directoryService.create(uuid, projectId, req);
        }
    }

    @Override
    public FileRespDto getFileContent(Long projectId, String path) throws IOException {
        return fileService.getFile(projectId, path);
    }

    @Override
    public void updateFileContent(Long projectId, FileUpdateReqDto dto) throws IOException {
        fileService.update(projectId, dto);
    }

    @Override
    public DirectoryTreeRespDto getTree(Long projectId) {
        return directoryService.view(projectId);
    }

    @Override
    public void rename(Long projectId, NodeRenameReqDto dto) throws IOException {
        if (dto.getType() == NodeType.FILE) {
            FileRenameReqDto req = new FileRenameReqDto();
            req.setCurrentPath(dto.getCurrentPath());
            req.setNewName(dto.getNewName());
            fileService.rename(projectId, req);
        } else {
            DirectoryRenameReqDto req = new DirectoryRenameReqDto();
            req.setCurrentPath(dto.getCurrentPath());
            req.setNewName(dto.getNewName());
            directoryService.rename(projectId, req);
        }
    }

    @Override
    public void move(Long projectId, NodeMoveReqDto dto) throws IOException {
        if (dto.getType() == NodeType.FILE) {
            FileMoveReqDto req = new FileMoveReqDto();
            req.setCurrentPath(dto.getCurrentPath());
            req.setNewParentDir(dto.getNewParentDir());
            fileService.move(projectId, req);
        } else {
            DirectoryMoveReqDto req = new DirectoryMoveReqDto();
            req.setCurrentPath(dto.getCurrentPath());
            req.setNewParentDir(dto.getNewParentDir());
            directoryService.move(projectId, req);
        }
    }

    @Override
    public void delete(Long projectId, String path, NodeType type) throws IOException {
        if (type == NodeType.FILE) {
            fileService.delete(projectId, path);
        } else {
            directoryService.delete(projectId, path);
        }
    }
}
