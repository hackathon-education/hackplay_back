package com.hackplay.hackplay.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.hackplay.hackplay.common.BaseException;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.domain.Member;
import com.hackplay.hackplay.dto.MemberReqDto;
import com.hackplay.hackplay.dto.ImageUploadRespDto;
import com.hackplay.hackplay.dto.MemberProfileRespDto;
import com.hackplay.hackplay.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    @Value("${file.upload-path:./uploads/profile/}") 
    private String uploadPath;

    @Value("${file.upload-url:/images/profile/}")
    private String uploadUrl;
    
    // 회원 정보 조회
    @Override
    public MemberProfileRespDto getMemberInfo(String uuid) {
        Member member = memberRepository.findByUuid(uuid)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS));
        
                return MemberProfileRespDto.from(
                        member.getEmail(),
                        member.getNickname(),
                        member.isEmailVerified(),
                        member.getProfileImageUrl(),
                        member.getRole(),
                        member.getStatus(),
                        member.getLastLoginAt(),
                        member.getCreatedAt(),
                        member.getUpdatedAt()
                );
    }

    @Override
    @Transactional
    public void updateMemberInfo(String uuid, MemberReqDto memberReqDto) {
        Member member = memberRepository.findByUuid(uuid)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS));
        
        // 2. 에러 해결: Role.valueOf()는 String을 인자로 받습니다. 
        // memberReqDto.getRole()이 이미 Role 타입이라면 바로 넣고, String이라면 아래처럼 사용하세요.
        member.updateMemberInfo(memberReqDto.getNickname(), memberReqDto.getProfileImageUrl(), memberReqDto.getRole());
    }

    @Override
    @Transactional
    public ImageUploadRespDto uploadProfileImage(MultipartFile file, String uuid) throws IOException {
        Member member = memberRepository.findByUuid(uuid)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS));

        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String extension = getFileExtension(file.getOriginalFilename());
        String filename = "profile_" + uuid + "_" + System.currentTimeMillis() + extension;
        Path filePath = uploadDir.resolve(filename);

        deleteOldProfileImage(member.getProfileImageUrl());

        Files.write(filePath, file.getBytes());

        String imageUrl = uploadUrl + filename;
        member.updateProfileImageUrl(imageUrl);

        return ImageUploadRespDto.builder()
                .imageUrl(imageUrl)
                .build();
    }

    // 헬퍼 메서드: 기존 파일 삭제 로직
    private void deleteOldProfileImage(String oldImageUrl) {
        if (oldImageUrl == null || !oldImageUrl.startsWith(uploadUrl)) {
            return;
        }

        try {
            String filename = oldImageUrl.substring(oldImageUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadPath).resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // 삭제 실패는 핵심 로직이 아니므로 로그만 남기거나 예외를 무시해도 됩니다.
            System.err.println("기존 프로필 이미지 삭제 실패: " + e.getMessage());
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return ".jpg";
        return fileName.substring(fileName.lastIndexOf("."));
    }

    @Override
    @Transactional
    public void deleteProfileImage(Long memberId, String uuid) throws IOException {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS));
        
        String filename = member.getProfileImageUrl().substring(member.getProfileImageUrl().lastIndexOf("/") + 1);
        Path filePath = Paths.get(uploadPath).resolve(filename);

        Files.deleteIfExists(filePath);
        member.updateProfileImageUrl(null);

    }

    @Override
    @Transactional
    public void deleteMemberInfo(String uuid) {
        Member member = memberRepository.findByUuid(uuid)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS));
        memberRepository.delete(member);
    }
    
}
