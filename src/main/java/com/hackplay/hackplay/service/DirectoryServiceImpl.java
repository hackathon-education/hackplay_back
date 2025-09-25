package com.hackplay.hackplay.service;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackplay.hackplay.common.BaseException;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.dto.DirectoryCreateReqDto;
import com.hackplay.hackplay.dto.DirectoryTreeRespDto;
import com.hackplay.hackplay.dto.DirectoryUpdateReqDto;
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
    @CacheEvict(value = "dirTree", key = "#projectId")
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
    @Cacheable(value = "dirTree", key = "#projectId")
    public DirectoryTreeRespDto view(Long projectId) {
        String projectUuid = projectRepository.findById(projectId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND))
                .getUuid();

        Path rootPath = Paths.get(BASE_PATH, projectUuid);
        if (!Files.exists(rootPath)) {
            throw new BaseException(BaseResponseStatus.DIRECTORY_NOT_FOUND);
        }
        return buildTree(rootPath);
    }

    private DirectoryTreeRespDto buildTree(Path dirPath) {
        try {
            List<DirectoryTreeRespDto> children = Files.list(dirPath)
                    .map(path -> {
                        if (Files.isDirectory(path)) {
                            return buildTree(path);
                        } else {
                            return new DirectoryTreeRespDto(
                                    path.getFileName().toString(),
                                    path.toString(),
                                    "FILE",
                                    List.of()
                            );
                        }
                    })
                    .collect(Collectors.toList());

            return new DirectoryTreeRespDto(
                    dirPath.getFileName().toString(),
                    dirPath.toString(),
                    "DIRECTORY",
                    children
            );
        } catch (IOException e) {
            throw new BaseException(BaseResponseStatus.DIRECTORY_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "dirTree", key = "#projectId")
    public void update(Long projectId, DirectoryUpdateReqDto dto) throws IOException {
        String projectUuid = projectRepository.findById(projectId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND))
                .getUuid();

        Path oldPath = Paths.get(BASE_PATH, projectUuid, dto.getOldPath());
        Path newPath = Paths.get(BASE_PATH, projectUuid, dto.getNewPath());

        if (!Files.exists(oldPath)) {
            throw new BaseException(BaseResponseStatus.DIRECTORY_NOT_FOUND);
        }

        if (Files.exists(newPath)) {
            throw new BaseException(BaseResponseStatus.DUPLICATE_DIRECTORY_NAME);
        }

        if (newPath.getParent() != null) {
            Files.createDirectories(newPath.getParent());
        }

        FileUtils.moveDirectory(oldPath.toFile(), newPath.toFile());
    }

    @Override
    @Transactional
    @CacheEvict(value = "dirTree", key = "#projectId")
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
