package com.hackplay.hackplay.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackplay.hackplay.common.BaseException;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.dto.DirectoryCreateReqDto;
import com.hackplay.hackplay.dto.DirectoryTreeRespDto;
import com.hackplay.hackplay.dto.DirectoryMoveReqDto;
import com.hackplay.hackplay.dto.DirectoryRenameReqDto;
import com.hackplay.hackplay.repository.MemberRepository;
import com.hackplay.hackplay.repository.ProjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectoryServiceImpl implements DirectoryService {

    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;

    @Value("${projects.base-path}")
    private String BASE_PATH;

    @Override
    @Transactional
    public void create(Long projectId, DirectoryCreateReqDto directoryCreateReqDto) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String uuid = (String) authentication.getPrincipal();

        memberRepository.findByUuid(uuid)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS));

        String projectUuid = projectRepository.findById(projectId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND))
                .getUuid();

        Path basePath = Paths.get(BASE_PATH, projectUuid);
        Path targetPath;

        if (directoryCreateReqDto.getParentPath() == null || directoryCreateReqDto.getParentPath().isBlank()) {
            targetPath = basePath.resolve(directoryCreateReqDto.getName());
        } else {
            targetPath = basePath.resolve(directoryCreateReqDto.getParentPath())
                                 .resolve(directoryCreateReqDto.getName());
        }

        Files.createDirectories(targetPath);
    }

    @Override
    public DirectoryTreeRespDto view(Long projectId) {
        String projectUuid = projectRepository.findById(projectId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND))
                .getUuid();

        Path rootPath = Paths.get(BASE_PATH, projectUuid);
        if (!Files.exists(rootPath)) {
            throw new BaseException(BaseResponseStatus.DIRECTORY_NOT_FOUND);
        }
        return buildTree(rootPath, rootPath);
    }

    private DirectoryTreeRespDto buildTree(Path dirPath, Path rootPath) {
        try {
            List<DirectoryTreeRespDto> children = Files.list(dirPath)
                    .map(path -> {
                        if (Files.isDirectory(path)) {
                            return buildTree(path, rootPath);
                        } else {
                            return new DirectoryTreeRespDto(
                                    path.getFileName().toString(),
                                    rootPath.relativize(path).toString().replace("\\", "/"), // ✅ 상대경로
                                    "FILE",
                                    List.of()
                            );
                        }
                    })
                    .collect(Collectors.toList());

            return new DirectoryTreeRespDto(
                    dirPath.equals(rootPath) ? "" : rootPath.relativize(dirPath).toString().replace("\\", "/"), // ✅ 루트면 빈 문자열
                    dirPath.equals(rootPath) ? "" : rootPath.relativize(dirPath).toString().replace("\\", "/"),
                    "DIRECTORY",
                    children
            );
        } catch (IOException e) {
            throw new BaseException(BaseResponseStatus.DIRECTORY_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void rename(Long projectId, DirectoryRenameReqDto directoryRenameReqDto) throws IOException {
        String projectUuid = projectRepository.findById(projectId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND))
                .getUuid();

        Path basePath = Paths.get(BASE_PATH, projectUuid);
        Path source = basePath.resolve(directoryRenameReqDto.getCurrentPath()).normalize();
        Path target = source.resolveSibling(directoryRenameReqDto.getNewName()).normalize();

        performMove(basePath, source, target);
    }

    @Override
    @Transactional
    public void move(Long projectId, DirectoryMoveReqDto directoryMoveReqDto) throws IOException {
        String projectUuid = projectRepository.findById(projectId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND))
                .getUuid();

        Path basePath = Paths.get(BASE_PATH, projectUuid);
        Path source = basePath.resolve(directoryMoveReqDto.getCurrentPath()).normalize();
        Path newParent = basePath.resolve(directoryMoveReqDto.getNewParentDir()).normalize();
        Path target = newParent.resolve(source.getFileName()).normalize();

        performMove(basePath, source, target);
    }

    private void performMove(Path basePath, Path source, Path target) throws IOException {
        if (!source.startsWith(basePath) || !target.startsWith(basePath)) {
            throw new SecurityException("허용되지 않은 경로 접근입니다.");
        }

        if (!Files.exists(source)) {
            throw new FileNotFoundException("대상 디렉토리가 존재하지 않습니다: " + source);
        }

        Files.createDirectories(target.getParent());
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    @Transactional
    public void delete(Long projectId, String dirPath) throws IOException {
        String projectUuid = projectRepository.findById(projectId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND))
                .getUuid();

        Path target = Paths.get(BASE_PATH, projectUuid, dirPath);

        if (!Files.exists(target)) {
            throw new BaseException(BaseResponseStatus.DIRECTORY_NOT_FOUND);
        }

        Path projectRoot = Paths.get(BASE_PATH, projectUuid);
        if (target.equals(projectRoot)) {
            throw new BaseException(BaseResponseStatus.CANNOT_DELETE_ROOT);
        }

        Files.walk(target)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {}
                });
    }
}
