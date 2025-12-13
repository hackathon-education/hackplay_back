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

    private final TerminalWebSocketHandler terminalHandler;
    private final RunWebSocketHandler runHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        /* 기존 PTY 터미널 */
        registry.addHandler(terminalHandler, "/ws/terminal")
                .setAllowedOrigins("*");

        /* 실행 로그 터미널 */
        registry.addHandler(runHandler, "/ws/run")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(
                            ServerHttpRequest request,
                            ServerHttpResponse response,
                            WebSocketHandler wsHandler,
                            Map<String, Object> attributes) throws Exception {

                        String query = request.getURI().getQuery();
                        log.info("🔍 WebSocket handshake - URI: {}, Query: {}", 
                                request.getURI(), query);

                        if (query != null && query.contains("projectId=")) {
                            String projectId = query.split("projectId=")[1].split("&")[0];
                            attributes.put("projectId", projectId);
                            log.info("✅ projectId extracted: {}", projectId);
                        } else {
                            log.warn("⚠️ projectId not found in query string");
                        }

                        return true;
                    }

                    @Override
                    public void afterHandshake(
                            ServerHttpRequest request,
                            ServerHttpResponse response,
                            WebSocketHandler wsHandler,
                            Exception exception) {
                        if (exception != null) {
                            log.error("Handshake error: ", exception);
                        }
                    }
                })
                .setAllowedOrigins("*");
    }
}