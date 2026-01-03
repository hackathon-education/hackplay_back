package com.hackplay.hackplay.config.webSocket;

import com.hackplay.hackplay.config.jwt.TokenProvider;
import com.hackplay.hackplay.service.ProjectWorkspaceService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.nio.file.Path;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtProjectHandshakeInterceptor implements HandshakeInterceptor {

    private final TokenProvider tokenProvider;
    private final ProjectWorkspaceService workspaceService;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        log.info("WS handshake start: {}", request.getURI());
        // ===== 1. projectId 쿼리 파라미터 =====
        String projectIdStr = getQueryParam(request, "projectId");
        log.info("projectId param = {}", projectIdStr);
        if (!StringUtils.hasText(projectIdStr)) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }

        long projectId;
        try {
            projectId = Long.parseLong(projectIdStr);
        } catch (NumberFormatException e) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }

        // ===== 2. accessToken 쿠키 추출 =====
        HttpServletRequest servletRequest =
                ((ServletServerHttpRequest) request).getServletRequest();

        String accessToken = getCookie(servletRequest, "accessToken");
        log.info("accessToken present = {}", accessToken != null);
        if (!StringUtils.hasText(accessToken)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        // ===== 3. accessToken 검증 =====
        if (!tokenProvider.validateToken(accessToken, false)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        // ===== 4. uuid 추출 =====
        String uuid = tokenProvider.getClaims(accessToken).getSubject();
        log.info("uuid = {}", uuid);
        if (!StringUtils.hasText(uuid)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        // ===== 5. 프로젝트 접근 권한 + root 경로 검증 =====
        Path projectRoot;
        try {
            projectRoot = workspaceService.resolveProjectRoot(projectId, uuid);
            log.info("projectRoot resolved = {}", projectRoot);
        } catch (Exception e) {
            log.error("❌ resolveProjectRoot failed: projectId={}, uuid={}", projectId, uuid, e);
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return false;
        }

        // ===== 6. WebSocket 세션 속성에 저장 =====
        attributes.put("uuid", uuid);
        attributes.put("projectId", projectId);
        attributes.put("projectRoot", projectRoot);

        log.info("WS handshake success");
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // no-op
    }

    // ================== Util ==================

    private static String getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    private static String getQueryParam(ServerHttpRequest request, String key) {
        String query = request.getURI().getRawQuery();
        if (!StringUtils.hasText(query)) return null;

        Map<String, String> map = parseQuery(query);
        return map.get(key);
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> map = new HashMap<>();
        String[] pairs = rawQuery.split("&");
        for (String pair : pairs) {
            if (!StringUtils.hasText(pair)) continue;

            String[] kv = pair.split("=", 2);
            String k = urlDecode(kv[0]);
            String v = (kv.length == 2) ? urlDecode(kv[1]) : "";
            map.put(k, v);
        }
        return map;
    }

    private static String urlDecode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
}
