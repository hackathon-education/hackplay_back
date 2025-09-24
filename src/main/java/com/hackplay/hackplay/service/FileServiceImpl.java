package com.hackplay.hackplay.service;

import com.hackplay.hackplay.common.BaseException;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.dto.FileCreateReqDto;
import com.hackplay.hackplay.dto.FileRespDto;
import com.hackplay.hackplay.dto.FileUpdateReqDto;
import com.hackplay.hackplay.repository.MemberRepository;
import com.hackplay.hackplay.repository.ProjectRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileServiceImpl implements FileService {

    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;

    @Value("${projects.base-path}")
    private String BASE_PATH;

    @Override
    @Transactional
    public void create(Long projectId, FileCreateReqDto fileCreateReqDto) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String uuid = (String) authentication.getPrincipal();

        memberRepository.findByUuid(uuid)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS));

        String projectUuid = projectRepository.findById(projectId).orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND)).getUuid();

        Path basePath = Paths.get(BASE_PATH, projectUuid);
        Path dirPath = (fileCreateReqDto.getParentPath() == null || fileCreateReqDto.getParentPath().isBlank())
                ? basePath
                : basePath.resolve(fileCreateReqDto.getParentPath());

        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        Path filePath = dirPath.resolve(fileCreateReqDto.getName());

        if (Files.exists(filePath)) {
            throw new BaseException(BaseResponseStatus.DUPLICATE_FILE_NAME);
        }

        Files.writeString(filePath, fileCreateReqDto.getContent(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW);

        long size = fileCreateReqDto.getContent().getBytes(StandardCharsets.UTF_8).length;
        if (size > 1048576) {
            throw new BaseException(BaseResponseStatus.FILE_SIZE_EXCEEDED);
        }
    }

    @Override
    public FileRespDto getFile(Long projectId, String path) throws IOException {
        String projectUuid = projectRepository.findById(projectId).orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND)).getUuid();
        Path filePath = Paths.get(BASE_PATH, projectUuid, path);
        if (!Files.exists(filePath)) {
            throw new BaseException(BaseResponseStatus.FILE_NOT_FOUND);
        }

        String content = Files.readString(filePath, StandardCharsets.UTF_8);
        return FileRespDto.from(filePath, content);
    }

    @Override
    @Transactional
    public void update(Long projectId, FileUpdateReqDto fileUpdateReqDto) throws IOException {
        String projectUuid = projectRepository.findById(projectId).orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND)).getUuid();
        Path filePath = Paths.get(BASE_PATH, projectUuid, fileUpdateReqDto.getPath());
        if (!Files.exists(filePath)) {
            throw new BaseException(BaseResponseStatus.FILE_NOT_FOUND);
        }

        Files.writeString(filePath, fileUpdateReqDto.getContent(), StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING);

        long newSize = fileUpdateReqDto.getContent().getBytes(StandardCharsets.UTF_8).length;
        if (newSize > 1048576) {
            throw new BaseException(BaseResponseStatus.FILE_SIZE_EXCEEDED);
        }
    }

    @Override
    @Transactional
    public void delete(Long projectId, String path) throws IOException {
        String projectUuid = projectRepository.findById(projectId).orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND)).getUuid();
        Path filePath = Paths.get(BASE_PATH, projectUuid, path);
        if (!Files.exists(filePath)) {
            throw new BaseException(BaseResponseStatus.FILE_NOT_FOUND);
        }
        Files.deleteIfExists(filePath);
    }
}
