package com.hackplay.hackplay.config.webSocket;

import com.hackplay.hackplay.config.jwt.TokenProvider;
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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtProjectHandshakeInterceptor implements HandshakeInterceptor {

    private final TokenProvider tokenProvider;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {

        // ===============================
        // 1. projectId (필수)
        // ===============================
        String projectIdStr = getQueryParam(request, "projectId");
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

        // ===============================
        // 2. accessToken (쿠키)
        // ===============================
        HttpServletRequest servletRequest =
                ((ServletServerHttpRequest) request).getServletRequest();

        String accessToken = getCookie(servletRequest, "accessToken");
        if (!StringUtils.hasText(accessToken)
                || !tokenProvider.validateToken(accessToken, false)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        // ===============================
        // 3. uuid (JWT subject)
        // ===============================
        String uuid = tokenProvider.getClaims(accessToken).getSubject();
        if (!StringUtils.hasText(uuid)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        // ===============================
        // 4. projectType (실행 템플릿용)
        //    ex) REACT / SPRING / PYTHON
        // ===============================
        String projectType = getQueryParam(request, "projectType");
        if (!StringUtils.hasText(projectType)) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }

        // ===============================
        // 5. WebSocket attributes 저장
        // ===============================
        attributes.put("uuid", uuid);
        attributes.put("projectId", projectId);
        attributes.put("projectType", projectType);

        log.debug(
            "WS handshake success: uuid={}, projectId={}, projectType={}",
            uuid, projectId, projectType
        );

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
