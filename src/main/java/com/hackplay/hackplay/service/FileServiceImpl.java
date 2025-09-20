package com.hackplay.hackplay.service;

import com.hackplay.hackplay.common.BaseException;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.domain.Directory;
import com.hackplay.hackplay.domain.File;
import com.hackplay.hackplay.domain.Member;
import com.hackplay.hackplay.domain.Project;
import com.hackplay.hackplay.dto.FileCreateReqDto;
import com.hackplay.hackplay.dto.FileRespDto;
import com.hackplay.hackplay.dto.FileUpdateReqDto;
import com.hackplay.hackplay.repository.DirectoryRepository;
import com.hackplay.hackplay.repository.FileRepository;
import com.hackplay.hackplay.repository.MemberRepository;
import com.hackplay.hackplay.repository.ProjectRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final MemberRepository memberRepository;
    private final DirectoryRepository directoryRepository;
    private final ProjectRepository projectRepository;
    private static final String BASE_PATH = "hackplay_back"; // TODO: 나중에 실제 프로젝트 경로로 변경 필요

    @Override
    @Transactional
    public void create(Long projectId, FileCreateReqDto dto) throws IOException {
        Path dirPath = Paths.get(BASE_PATH, projectId.toString());
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String uuid = (String) authentication.getPrincipal();

        Member member = memberRepository.findByUuid(uuid)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS));

        Path filePath = dirPath.resolve(dto.getName());

        // 중복 체크 (DB + 파일시스템)
        if (Files.exists(filePath) ||
            fileRepository.findByProjectIdAndName(projectId, dto.getName()).isPresent()) {
            throw new BaseException(BaseResponseStatus.FILE_ALREADY_EXISTS);
        }

        // 파일 생성
        Files.writeString(filePath, dto.getContent(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW);

        long size = dto.getContent().getBytes(StandardCharsets.UTF_8).length;
        if (size > 1048576) { // TODO: 현재 1MB 제한 -> 추후 파일 최대 용량 결정 필요
            throw new BaseException(BaseResponseStatus.FILE_SIZE_EXCEEDED);
        }

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND));
        Directory directory = directoryRepository.findById(dto.getDirId()).orElseThrow(() -> new BaseException(BaseResponseStatus.DIRECTORY_NOT_FOUND));

        File file = File.builder()
                .project(project)
                .dir(directory)
                .member(member)
                .name(dto.getName())
                .path(filePath.toString())
                .size(size)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        fileRepository.save(file);
    }

    @Override
    public FileRespDto getFile(Long projectId, Long fileId) throws IOException{
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FILE_NOT_FOUND));

        Path filePath = Paths.get(file.getPath());
        String content = Files.readString(filePath, StandardCharsets.UTF_8);

        return FileRespDto.entityToDto(file, content);
    }

    @Override
    @Transactional
    public void update(Long projectId, Long fileId, FileUpdateReqDto dto) throws IOException {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FILE_NOT_FOUND));

        Path filePath = Paths.get(file.getPath());
        Files.writeString(filePath, dto.getContent(), StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING);

        long newSize = dto.getContent().getBytes(StandardCharsets.UTF_8).length;
        if (newSize > 1048576) {
            throw new BaseException(BaseResponseStatus.FILE_SIZE_EXCEEDED);
        }

        file.updateFile(file.getPath(), newSize);
        fileRepository.save(file);
    }

    @Override
    @Transactional
    public void delete(Long projectId, Long fileId) throws IOException {

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FILE_NOT_FOUND));

        Files.deleteIfExists(Paths.get(file.getPath()));
        fileRepository.delete(file);
    }
}
