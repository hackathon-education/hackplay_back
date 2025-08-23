package com.hackplay.hackplay.config.jwt;

import java.util.Enumeration;

import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class AuthorizationExtractor {

    public static final String AUTHORIZATION = "Authorization";
    public static final String REFRESH_TOKEN_HEADER = "Refresh";

    public String extract(HttpServletRequest request, String type) {
        Enumeration<String> headers = request.getHeaders(AUTHORIZATION);
        while (headers.hasMoreElements()) {
            String value = headers.nextElement();
            if (value.toLowerCase().startsWith(type.toLowerCase())) {
                return value.substring(type.length()).trim();
            }
        }

        return Strings.EMPTY;
    }

    public String extractRefreshToken(HttpServletRequest request) {
        return request.getHeader(REFRESH_TOKEN_HEADER);
    }
}
