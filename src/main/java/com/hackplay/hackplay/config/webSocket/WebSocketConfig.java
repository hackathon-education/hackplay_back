package com.hackplay.hackplay.config.webSocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final LinuxTerminalWebSocketHandler linuxTerminalHandler;
    private final RunWebSocketHandler runWebSocketHandler;
    private final JwtProjectHandshakeInterceptor jwtProjectHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        // ===============================
        // 웹 터미널 (bash interactive)
        // ===============================
        registry.addHandler(linuxTerminalHandler, "/ws/terminal")
                .addInterceptors(jwtProjectHandshakeInterceptor)
                .setAllowedOrigins(
                        "https://hackplay.co.kr",
                        "https://www.hackplay.co.kr"
                );

        // ===============================
        // 프로젝트 실행 로그 터미널
        // ===============================
        registry.addHandler(runWebSocketHandler, "/ws/run")
                .addInterceptors(jwtProjectHandshakeInterceptor)
                .setAllowedOrigins(
                        "https://hackplay.co.kr",
                        "https://www.hackplay.co.kr"
                );
    }
}
