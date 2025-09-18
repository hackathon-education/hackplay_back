package com.hackplay.hackplay.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackplay.hackplay.common.BaseException;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.domain.Directory;
import com.hackplay.hackplay.domain.Member;
import com.hackplay.hackplay.domain.Project;
import com.hackplay.hackplay.dto.DirectoryCreateReqDto;
import com.hackplay.hackplay.dto.DirectoryTreeRespDto;
import com.hackplay.hackplay.dto.DirectoryUpdateReqDto;
import com.hackplay.hackplay.repository.DirectoryRepository;
import com.hackplay.hackplay.repository.MemberRepository;
import com.hackplay.hackplay.repository.ProjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectoryServiceImpl implements DirectoryService {

    private final DirectoryRepository directoryRepository;
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;

    private static final String BASE_PATH = "projects";

    @Override
    @Transactional
    public void create(Long projectId, DirectoryCreateReqDto directoryCreateReqDto) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String uuid = (String) authentication.getPrincipal();

        Member member = memberRepository.findByUuid(uuid)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS));

        Long parentId = directoryCreateReqDto.getParentId();

        // 루트 디렉토리 중복 체크
        if (parentId == null && directoryRepository.existsByProjectIdAndParentIdIsNull(projectId)) {
            throw new BaseException(BaseResponseStatus.ROOT_DIRECTORY_ALREADY_EXISTS);
        }

        // 상위 디렉토리 존재 여부 체크
        if (parentId != null && !directoryRepository.existsById(parentId)) {
            throw new BaseException(BaseResponseStatus.PARENT_DIRECTORY_NOT_FOUND);
        }

        // 동일 부모 아래 중복 이름 방지 체크
        if (directoryRepository.existsByProjectIdAndParentIdAndName(projectId, parentId, directoryCreateReqDto.getName())) {
            throw new BaseException(BaseResponseStatus.DUPLICATE_DIRECTORY_NAME);
        }

        String path = makePath(projectId, parentId, directoryCreateReqDto.getName());

        Files.createDirectories(Paths.get(path));

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND));

        Directory directory = Directory.builder()
                .project(project)
                .parentId(parentId)
                .member(member)
                .name(directoryCreateReqDto.getName())
                .path(path)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        directoryRepository.save(directory);
    }

    private String makePath(Long projectId, Long parentId, String name) {
        if (parentId == null) {
            return Paths.get(BASE_PATH, String.valueOf(projectId), name).toString();
        }
        Directory parent = directoryRepository.findById(parentId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PARENT_DIRECTORY_NOT_FOUND));
        return Paths.get(parent.getPath(), name).toString();
    }


    @Override
    public DirectoryTreeRespDto view(Long projectId, Long dirId) {
        Directory root = directoryRepository.findByIdAndProjectId(dirId, projectId).orElseThrow(() -> new BaseException(BaseResponseStatus.DIRECTORY_NOT_FOUND));

        return buildTree(root);
    }

    private DirectoryTreeRespDto buildTree(Directory dir){
        List<Directory> children = directoryRepository.findByParentId(dir.getId());

        List<DirectoryTreeRespDto> childNodes = children.stream()
            .map(this::buildTree)
            .toList();

        return new DirectoryTreeRespDto(
                dir.getId(),
                dir.getName(),
                dir.getPath(),
                dir.getParentId(),
                childNodes
        );
    }

    @Override
    @Transactional
    public void update(Long projectId, Long dirId, DirectoryUpdateReqDto directoryUpdateReqDto) throws IOException {

        Directory dir = directoryRepository.findByIdAndProjectId(dirId, projectId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.DIRECTORY_NOT_FOUND));

        String newName = directoryUpdateReqDto.getNewName();
        Long newParentId = directoryUpdateReqDto.getNewParentId();

        String oldPath = dir.getPath();
        String newPath = makePath(projectId, newParentId, newName);

        Files.move(Paths.get(oldPath), Paths.get(newPath), StandardCopyOption.ATOMIC_MOVE);

        dir.updateDirNameAndPath(newName, newParentId, newPath);
        directoryRepository.save(dir);

        updateChildPaths(dir.getId(), oldPath, newPath);
    }

    private void updateChildPaths(Long parentId, String oldParentPath, String newParentPath) {
        List<Directory> children = directoryRepository.findByParentId(parentId);

        for (Directory child : children) {
            String updatedPath = child.getPath().replace(oldParentPath, newParentPath);

            child.updateDirPath(updatedPath);
            directoryRepository.save(child);

            updateChildPaths(child.getId(), child.getPath(), updatedPath);
        }
    }

    @Override
    @Transactional
    public void delete(Long projectId, Long dirId) throws IOException {
        Directory dir = directoryRepository.findByIdAndProjectId(dirId, projectId).orElseThrow(() -> new BaseException(BaseResponseStatus.DIRECTORY_NOT_FOUND));

        if (dir.getParentId() == null) throw new BaseException(BaseResponseStatus.CANNOT_DELETE_ROOT);

        Path target = Paths.get(dir.getPath());
        if (Files.exists(target)) {
            Files.walk(target)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {}
                    });
        }
        
        deleteRecursively(dir.getId());

        directoryRepository.delete(dir);
    }

    // 디렉토리 재귀 삭제
    private void deleteRecursively(Long parentId){
        List<Directory> children = directoryRepository.findByParentId(parentId);

        for(Directory child: children){
            deleteRecursively(child.getId());
            directoryRepository.delete(child);
        }
    }
    
}