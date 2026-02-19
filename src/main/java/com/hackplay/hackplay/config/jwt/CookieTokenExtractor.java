package com.hackplay.hackplay.config.jwt;

import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CookieTokenExtractor {

    public static final String ACCESS_TOKEN = "accessToken";
    public static final String REFRESH_TOKEN = "refreshToken";

    public String extractAccessToken(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, ACCESS_TOKEN);
        return cookie != null ? cookie.getValue() : null;
    }

    public String extractRefreshToken(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, REFRESH_TOKEN);
        return cookie != null ? cookie.getValue() : null;
    }
}
