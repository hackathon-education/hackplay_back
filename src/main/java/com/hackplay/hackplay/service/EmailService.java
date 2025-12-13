package com.hackplay.hackplay.service;

import com.hackplay.hackplay.dto.EmailAuthReqDto;
import com.hackplay.hackplay.dto.EmailVerifyReqDto;

import jakarta.mail.internet.MimeMessage;

public interface EmailService {
    String createCode();
    String setEmail(String email, String code);
    MimeMessage createEmailForm(String email);
    void sendEmail(EmailAuthReqDto emailAuthReqDto);
    void verifyEmail(EmailVerifyReqDto emailVerifyReqDto);
    String checkDuplicateEmail(EmailAuthReqDto emailAuthReqDto);
}
