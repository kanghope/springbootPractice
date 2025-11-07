package com.shop.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.http.MediaType; // ⭐ 추가

import java.io.IOException;
import java.io.PrintWriter; // ⭐ 추가

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // ⭐ JSON 형식으로 응답하도록 수정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        PrintWriter writer = response.getWriter();
        // 실제 API 개발에서는 더 상세한 JSON 응답 구조를 사용합니다.
        writer.write("{\"error\": \"Unauthorized\", \"message\": \"인증되지 않은 사용자입니다. 토큰을 확인해주세요.\"}");
        writer.flush();
    }
}