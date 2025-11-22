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
    private final AuthorizationExtractor authExtractor;

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        log.info(">>> JwtAuthenticationFilter 호출: {}", path);

        if (shouldSkipFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;
        boolean isRefresh = false;

        if ("/api/v1/token/rotate".equals(path)) {
            token = authExtractor.extractRefreshToken(request);
            isRefresh = true;
        } else {
            String raw = authExtractor.extract(request, "Bearer");
            token = (raw != null) ? raw.replaceAll("\\s+", "") : null;
        }

        if (StringUtils.hasText(token) && !tokenProvider.validateToken(token, isRefresh)) {
            BaseException.sendErrorResponse(response, BaseResponseStatus.TOKEN_EXPIRED);
            return;
        }

        if (!isRefresh) { // Access Token만 인증 처리
            var claims = tokenProvider.getClaims(token);

            String uuid = claims.getSubject();
            String auth = claims.get("auth", String.class).toUpperCase();

            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + auth));

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(uuid, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            request.setAttribute("uuid", uuid);
        }

        filterChain.doFilter(request, response);
    }
    private boolean shouldSkipFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/")
            || path.startsWith("/api/v1/email/")
            || path.startsWith("/v3/api-docs")
            || path.startsWith("/swagger-ui")
            || path.equals("/swagger-ui.html")
            || path.startsWith("/editor/")
            || path.startsWith("/ws/")
            || path.startsWith("/login.html")
            || path.startsWith("/projects.html")
            || path.startsWith("/js/")
            || path.startsWith("/css/")
            || path.startsWith("/images/")
            || path.equals("/favicon.ico")
            || path.startsWith("/static/");
    }
}
