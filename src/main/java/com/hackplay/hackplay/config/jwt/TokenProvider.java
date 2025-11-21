package com.hackplay.hackplay.config.jwt;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.hackplay.hackplay.common.BaseException;
import com.hackplay.hackplay.common.BaseResponseStatus;
import com.hackplay.hackplay.domain.Member;
import com.hackplay.hackplay.repository.MemberRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;

@Component
public class TokenProvider {
    
    private final MemberRepository memberRepository;

    @Getter
    private final SecretKey signingKey;
    private final long accessTokenExpirationTime;
    private final long refreshTokenExpirationTime;

    public TokenProvider(MemberRepository memberRepository, 
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expiration-time}") long accessTokenExpirationTime,
            @Value("${jwt.refresh-token-expiration-time}") long refreshTokenExpirationTime) {
        secretKey = secretKey.replaceAll("\\s+", "");
        this.memberRepository = memberRepository;
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));
        this.accessTokenExpirationTime = accessTokenExpirationTime;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
    }

    // 액세스 토큰 생성
    public String createAccessToken(String uuid) {
        return createToken(uuid, accessTokenExpirationTime);
    }

    // 리프레쉬 토큰 생성
    public String createRefreshToken(String uuid) {
        return createToken(uuid, refreshTokenExpirationTime);
    }

    // 토큰 생성
    // 추후 role -> 관리자, 일반 회원 구분 칼럼으로 추가 및 변경 필요 -> auth로 변경완료
    private String createToken(String uuid, long expirationTime) {
        Member member = memberRepository.findByUuid(uuid)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS));
        String auth = member.getAuth().name();

        Date expiryDate = new Date(System.currentTimeMillis() + expirationTime);
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(uuid)
                .claim("auth", auth)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 검증
    public boolean validateToken(String token, boolean isRefresh) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

            if (!claims.getExpiration().after(new Date())) {
                return false;
            }

            if (isRefresh) {
                String uuid = claims.getSubject();
                Member member = memberRepository.findByUuid(uuid)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_MEMBERS));
                
                return token.equals(member.getRefreshToken());
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token){
        return Jwts.parserBuilder()
        .setSigningKey(signingKey)
        .build()
        .parseClaimsJws(token)
        .getBody();
    }
}
