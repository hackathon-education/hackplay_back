package com.hackplay.hackplay.service;

import com.hackplay.hackplay.dto.MemberReqDto;
import com.hackplay.hackplay.dto.MemberUpdatePWReqDto;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.hackplay.hackplay.dto.ImageUploadRespDto;
import com.hackplay.hackplay.dto.MemberProfileRespDto;

public interface MemberService {

    MemberProfileRespDto getMemberInfo(String uuid);
    void updateMemberInfo(String uuid, MemberReqDto memberReqDto);
    void updateMemberPassword(String uuid, MemberUpdatePWReqDto memberUpdatePWReqDto);

    ImageUploadRespDto uploadProfileImage(MultipartFile file, String uuid)  throws IOException;

    void deleteProfileImage(Long memberId, String uuid) throws IOException;
    void deleteMemberInfo(String uuid);
}
