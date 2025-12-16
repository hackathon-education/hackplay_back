package com.hackplay.hackplay.config.webSocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final LinuxTerminalWebSocketHandler linuxTerminalHandler;
    private final RunWebSocketHandler runHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        /* 리눅스 네이티브 터미널 (pty4j 없이) */
        registry.addHandler(linuxTerminalHandler, "/ws/terminal")
                .addInterceptors(new TerminalHandshakeInterceptor())
                .setAllowedOrigins("*")
                .withSockJS(); // SockJS fallback 지원

        /* 프로젝트 실행 로그 터미널 */
        registry.addHandler(runHandler, "/ws/run")
                .addInterceptors(new ProjectHandshakeInterceptor())
                .setAllowedOrigins("*")
                .withSockJS(); // SockJS fallback 지원
    }

    /**
     * 터미널 연결용 HandshakeInterceptor
     */
    private static class TerminalHandshakeInterceptor implements HandshakeInterceptor {
        
        @Override
        public boolean beforeHandshake(
                ServerHttpRequest request,
                ServerHttpResponse response,
                WebSocketHandler wsHandler,
                Map<String, Object> attributes) throws Exception {

            String query = request.getURI().getQuery();
            log.info("🔍 Terminal WebSocket handshake - URI: {}, Query: {}", 
                    request.getURI(), query);

            // 터미널 크기 정보 추출 (선택사항)
            if (query != null) {
                extractQueryParam(query, "cols", attributes);
                extractQueryParam(query, "rows", attributes);
                extractQueryParam(query, "workDir", attributes);
            }

            // 기본값 설정
            attributes.putIfAbsent("cols", "120");
            attributes.putIfAbsent("rows", "30");
            attributes.putIfAbsent("workDir", System.getProperty("user.dir"));

            return true;
        }

        @Override
        public void afterHandshake(
                ServerHttpRequest request,
                ServerHttpResponse response,
                WebSocketHandler wsHandler,
                Exception exception) {
            if (exception != null) {
                log.error("Terminal handshake error: ", exception);
            } else {
                log.info("✅ Terminal handshake completed successfully");
            }
        }

        private void extractQueryParam(String query, String paramName, Map<String, Object> attributes) {
            if (query.contains(paramName + "=")) {
                try {
                    String value = query.split(paramName + "=")[1].split("&")[0];
                    attributes.put(paramName, value);
                    log.debug("✅ {} extracted: {}", paramName, value);
                } catch (Exception e) {
                    log.warn("⚠️ Failed to extract {}: {}", paramName, e.getMessage());
                }
            }
        }
    }

    /**
     * 프로젝트 실행용 HandshakeInterceptor
     */
    private static class ProjectHandshakeInterceptor implements HandshakeInterceptor {
        
        @Override
        public boolean beforeHandshake(
                ServerHttpRequest request,
                ServerHttpResponse response,
                WebSocketHandler wsHandler,
                Map<String, Object> attributes) throws Exception {

            String query = request.getURI().getQuery();
            log.info("🔍 Project WebSocket handshake - URI: {}, Query: {}", 
                    request.getURI(), query);

            if (query != null && query.contains("projectId=")) {
                String projectId = query.split("projectId=")[1].split("&")[0];
                attributes.put("projectId", projectId);
                log.info("✅ projectId extracted: {}", projectId);
                return true;
            } else {
                log.warn("⚠️ projectId not found in query string");
                response.setStatusCode(org.springframework.http.HttpStatus.BAD_REQUEST);
                return false; // projectId 없으면 연결 거부
            }
        }

        @Override
        public void afterHandshake(
                ServerHttpRequest request,
                ServerHttpResponse response,
                WebSocketHandler wsHandler,
                Exception exception) {
            if (exception != null) {
                log.error("Project handshake error: ", exception);
            } else {
                log.info("✅ Project handshake completed successfully");
            }
        }
    }
}