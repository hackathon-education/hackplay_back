package com.hackplay.hackplay.service;

import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackplay.hackplay.common.BaseException;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.domain.Directory;
import com.hackplay.hackplay.domain.Member;
import com.hackplay.hackplay.dto.DirectoryCreateReqDto;
import com.hackplay.hackplay.dto.DirectoryTreeRespDto;
import com.hackplay.hackplay.dto.DirectoryUpdateReqDto;
import com.hackplay.hackplay.repository.DirectoryRepository;
import com.hackplay.hackplay.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectoryServiceImpl implements DirectoryService {

    private final DirectoryRepository directoryRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void create(Long projectId, DirectoryCreateReqDto directoryCreateReqDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String uuid = (String) authentication.getPrincipal();

        Member member = memberRepository.findByUuid(uuid);
        if (member == null) {
            throw new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS);
        }

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

        Directory directory = Directory.builder()
                .projectId(projectId)
                .parentId(parentId)
                .member(member)
                .name(directoryCreateReqDto.getName())
                .path(makePath(projectId, directoryCreateReqDto.getParentId(), directoryCreateReqDto.getName()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        directoryRepository.save(directory);
    }

    private String makePath(Long projectId, Long parentId, String name) {
        if (parentId == null) {
            return String.format("/projects/%d/%s", projectId, name);
        }
        Directory parent = directoryRepository.findById(parentId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PARENT_DIRECTORY_NOT_FOUND));
        return parent.getPath() + "/" + name;
    }


    @Override
    public DirectoryTreeRespDto view(Long projectId, Long dirId) {
        throw new UnsupportedOperationException("Unimplemented method 'view'");
    }

    @Override
    public void update(Long projectId, Long dirId, DirectoryUpdateReqDto directoryUpdateReqDto) {
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void delete(Long projectId, Long dirId) {
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }


    
}