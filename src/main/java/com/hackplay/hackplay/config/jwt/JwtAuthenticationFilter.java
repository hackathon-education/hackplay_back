package com.hackplay.hackplay.config.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hackplay.hackplay.common.BaseException;
import com.hackplay.hackplay.common.BaseResponseStatus;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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

        String token;
        String tokenType;

        if ("/api/v1/token/rotate".equals(path)) {
            token = authExtractor.extractRefreshToken(request);
            tokenType = "refresh";
        } else {
            token = authExtractor.extract(request, "Bearer").replaceAll("\\s+", "");
            tokenType = "access";
        }

        if (StringUtils.hasText(token) && tokenProvider.validateToken(token, tokenType)) {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(tokenProvider.getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String uuid = claims.getSubject();
            String role = claims.get("role", String.class).toUpperCase();

            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
            log.info("권한 확인: {}", authorities);

            Authentication authentication = new UsernamePasswordAuthenticationToken(uuid, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute("uuid", uuid);
            filterChain.doFilter(request, response);
        } else {
            BaseException.sendErrorResponse(response, BaseResponseStatus.TOKEN_EXPIRED);
        }
    }
    private boolean shouldSkipFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/")
        ||     path.startsWith("/api/v1/email/");
    }
}
