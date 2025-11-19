package com.hackplay.hackplay.service;

import java.io.UnsupportedEncodingException;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.hackplay.hackplay.common.BaseException;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.config.redis.RedisUtil;
import com.hackplay.hackplay.dto.EmailAuthReqDto;
import com.hackplay.hackplay.dto.EmailVerifyReqDto;
import com.hackplay.hackplay.repository.MemberRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailServiceImpl implements EmailService{

    private final JavaMailSender javaMailSender;
    private final RedisUtil redisUtil;
    private final MemberRepository memberRepository;

    @Value("${mail.from-address}")
    private String fromAddress;

    @Override
    public String createCode() {
        int leftLimit = 48;
        int rightLimit = 122;
        int targetStringLength = 6;
        Random random = new Random();

        String code = random.ints(leftLimit, rightLimit + 1)
            .filter(i -> (i <= 57 || i >= 65) && (i <= 90 | i >= 97))
            .limit(targetStringLength)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();

        return code;
    }

    @Override
    public String setEmail(String email, String code) {
        Context context = new Context();
        context.setVariable("code", code);
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(false);

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        String emailContent = templateEngine.process("verifyEmailTemplate", context);

        return emailContent;
    }

    @Override
    public MimeMessage createEmailForm(String email) {
        // 인증 코드 생성
        String authCode = createCode();
        // 인증 코드로 이메일 내용 설정
        String emailContent = setEmail(email, authCode);

        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("Hackplay 인증코드 안내 메일");
            helper.setText(emailContent, true); 
            helper.setFrom(fromAddress, fromAddress);
        } catch (MessagingException e) {
            throw new RuntimeException("메일 생성 중 오류 발생", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        redisUtil.setDataExpire(email, authCode, 60 * 30L);
        return message;
    }

    @Override
    public void sendEmail(EmailAuthReqDto emailAuthReqDto) {
        if (redisUtil.existData(emailAuthReqDto.getEmail())) {
            redisUtil.deleteData(emailAuthReqDto.getEmail());
        }
        MimeMessage emailForm = createEmailForm(emailAuthReqDto.getEmail());
        try {
            javaMailSender.send(emailForm);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException(BaseResponseStatus.FAIL_MAIL_SEND);
        }
    }

    @Override
    @Transactional
    public void verifyEmail(EmailVerifyReqDto emailVerifyReqDto) {
        String findCodeByEmail = redisUtil.getData(emailVerifyReqDto.getEmail());
        if (findCodeByEmail == null) {
            throw new BaseException(BaseResponseStatus.UNMATCHED_EMAIL_AUTH_CODE);
        }

        if (!findCodeByEmail.equals(emailVerifyReqDto.getVerifyCode())) {
            throw new BaseException(BaseResponseStatus.INVALID_VERIFICATION_CODE);
        }
        redisUtil.setDataExpire(emailVerifyReqDto.getEmail() + ":verified", "true", 300L);
    }

    @Override
    public String checkDuplicateEmail(EmailAuthReqDto emailAuthReqDto) {
        if (memberRepository.existsByEmail(emailAuthReqDto.getEmail())){
            return "Y";
        } else{
            return "N";
        }
    }
    
}
