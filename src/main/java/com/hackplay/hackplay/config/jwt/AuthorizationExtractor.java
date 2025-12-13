package com.hackplay.hackplay.config.jwt;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class AuthorizationExtractor {

    public static final String AUTHORIZATION = "Authorization";
    public static final String REFRESH_TOKEN_HEADER = "Refresh";

    public String extract(HttpServletRequest request, String type) {
        String header = request.getHeader(AUTHORIZATION);
        if(header == null || !header.startsWith(type))
            return null;

        return header.substring(type.length()).trim();
    }

    public String extractRefreshToken(HttpServletRequest request) {
        String header = request.getHeader(REFRESH_TOKEN_HEADER);
        if (header == null){
            return null;
        }
        return header.trim();
    }
}
