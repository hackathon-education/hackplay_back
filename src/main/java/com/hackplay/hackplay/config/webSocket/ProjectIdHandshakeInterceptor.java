package com.hackplay.hackplay.config.webSocket;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@RequiredArgsConstructor
public class ProjectIdHandshakeInterceptor implements HandshakeInterceptor {

    private final String key;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        String query = request.getURI().getQuery(); // projectId=123
        if (query == null || !query.contains(key + "=")) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }

        try {
            String value = query.split(key + "=")[1].split("&")[0];
            attributes.put(key, value);
            return true;
        } catch (Exception e) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
