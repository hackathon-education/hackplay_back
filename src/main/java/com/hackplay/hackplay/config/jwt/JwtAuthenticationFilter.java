package com.hackplay.hackplay.config.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hackplay.hackplay.common.BaseException;
import com.hackplay.hackplay.common.BaseResponseStatus;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final TokenProvider tokenProvider;
    private final CookieTokenExtractor cookieTokenExtractor;

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        log.debug("JwtAuthenticationFilter path={}", path);

        if (shouldSkipFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = cookieTokenExtractor.extractAccessToken(request);

        // 토큰이 아예 없으면 → 인증 시도 없이 다음 필터로 넘김 (로그인 API 포함)
        if (!StringUtils.hasText(accessToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰이 있으나 잘못된 경우
        if (!tokenProvider.validateToken(accessToken, false)) {
            BaseException.sendErrorResponse(response, BaseResponseStatus.TOKEN_EXPIRED);
            return;
        }

        var claims = tokenProvider.getClaims(accessToken);

        String uuid = claims.getSubject();
        String auth = claims.get("auth", String.class).toUpperCase();

        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + auth));

        Authentication authentication = new UsernamePasswordAuthenticationToken(uuid, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private boolean shouldSkipFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // 인증 관련 (로그아웃 제외)
        if (path.startsWith("/api/v1/auth/") && !path.equals("/api/v1/auth/signout")) {
            return true;
        }

        // 이메일 인증
        if (path.startsWith("/api/v1/email/")) {
            return true;
        }

        // Swagger
        if (path.startsWith("/v3/api-docs")
            || path.startsWith("/swagger-ui")
            || path.equals("/swagger-ui.html")) {
            return true;
        }

        // 정적/기타
        if (path.startsWith("/editor/")
            || path.startsWith("/ws/")
            || path.equals("/login.html")
            || path.equals("/projects.html")
            || path.startsWith("/js/")
            || path.startsWith("/css/")
            || path.startsWith("/images/")
            || path.startsWith("/static/")
            || path.equals("/favicon.ico")) {
            return true;
        }

        return false;
    }
}
