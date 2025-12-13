package com.hackplay.hackplay.service;

import com.hackplay.hackplay.common.BaseException;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.dto.FileCreateReqDto;
import com.hackplay.hackplay.dto.FileMoveReqDto;
import com.hackplay.hackplay.dto.FileRenameReqDto;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileServiceImpl implements FileService {

    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;

    @Value("${projects.base-path}")
    private String BASE_PATH;

    // UUID 패턴 정규식
    private static final Pattern UUID_PATTERN = 
        Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", Pattern.CASE_INSENSITIVE);

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
                : basePath.resolve(cleanPath(fileCreateReqDto.getParentPath(), projectUuid));

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
        String projectUuid = projectRepository.findById(projectId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND))
                .getUuid();

        // 경로 정리
        if (path == null || path.isBlank()) {
            throw new BaseException(BaseResponseStatus.FILE_NOT_FOUND);
        }

        // 경로에서 프로젝트 UUID 제거
        String cleanedPath = cleanPath(path, projectUuid);
        
        // OS별 호환 슬래시 정규화
        String normalizedPath = cleanedPath.replace("\\", "/");

        Path filePath = Paths.get(BASE_PATH, projectUuid).resolve(normalizedPath).normalize();

        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            throw new BaseException(BaseResponseStatus.FILE_NOT_FOUND);
        }

        String content = Files.readString(filePath, StandardCharsets.UTF_8);
        return FileRespDto.from(filePath, content);
    }

    @Override
    @Transactional
    public void update(Long projectId, FileUpdateReqDto fileUpdateReqDto) throws IOException {
        String projectUuid = projectRepository.findById(projectId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND))
                .getUuid();
                
        // 경로에서 프로젝트 UUID 제거
        String cleanedPath = cleanPath(fileUpdateReqDto.getPath(), projectUuid);
        
        Path filePath = Paths.get(BASE_PATH, projectUuid, cleanedPath);
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
    public void rename(Long projectId, FileRenameReqDto fileRenameReqDto) throws IOException {
        String projectUuid = projectRepository.findById(projectId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND))
                .getUuid();

        Path basePath = Paths.get(BASE_PATH, projectUuid);
        Path source = basePath.resolve(cleanPath(fileRenameReqDto.getCurrentPath(), projectUuid)).normalize();
        Path target = source.resolveSibling(fileRenameReqDto.getNewName()).normalize();

        performMove(basePath, source, target);
    }

    @Override
    @Transactional
    public void move(Long projectId, FileMoveReqDto fileMoveReqDto) throws IOException {
        String projectUuid = projectRepository.findById(projectId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND))
                .getUuid();

        Path basePath = Paths.get(BASE_PATH, projectUuid);
        Path source = basePath.resolve(cleanPath(fileMoveReqDto.getCurrentPath(), projectUuid)).normalize();
        Path newParent = basePath.resolve(cleanPath(fileMoveReqDto.getNewParentDir(), projectUuid)).normalize();
        Path target = newParent.resolve(source.getFileName()).normalize();

        performMove(basePath, source, target);
    }

    private void performMove(Path basePath, Path source, Path target) throws IOException {
        if (!source.startsWith(basePath) || !target.startsWith(basePath))
            throw new SecurityException("허용되지 않은 경로 접근입니다.");

        if (!Files.exists(source))
            throw new FileNotFoundException("파일이 존재하지 않습니다: " + source);

        Files.createDirectories(target.getParent());
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    @Transactional
    public void delete(Long projectId, String path) throws IOException {
        String projectUuid = projectRepository.findById(projectId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PROJECT_NOT_FOUND))
                .getUuid();
                
        // 경로에서 프로젝트 UUID 제거
        String cleanedPath = cleanPath(path, projectUuid);
        
        Path filePath = Paths.get(BASE_PATH, projectUuid, cleanedPath);
        if (!Files.exists(filePath)) {
            throw new BaseException(BaseResponseStatus.FILE_NOT_FOUND);
        }
        Files.deleteIfExists(filePath);
    }
    
    /**
     * 경로에서 UUID를 제거하는 메서드
     * @param path 원본 경로
     * @param projectUuid 프로젝트 UUID
     * @return UUID가 제거된 경로
     */
    private String cleanPath(String path, String projectUuid) {
        if (path == null || path.isBlank()) {
            return "";
        }
        
        // 슬래시 정규화
        String normalized = path.replace("\\", "/");
        
        // 경로에 projectUuid가 있으면 제거
        if (normalized.startsWith(projectUuid + "/")) {
            return normalized.substring(projectUuid.length() + 1);
        }
        
        // 다른 UUID가 있는지 확인
        Matcher matcher = UUID_PATTERN.matcher(normalized);
        if (matcher.find()) {
            int uuidEndIndex = matcher.end();
            // UUID 이후에 슬래시가 있는지 확인
            if (normalized.length() > uuidEndIndex && normalized.charAt(uuidEndIndex) == '/') {
                return normalized.substring(uuidEndIndex + 1);
            }
        }
        
        return normalized;
    }
}