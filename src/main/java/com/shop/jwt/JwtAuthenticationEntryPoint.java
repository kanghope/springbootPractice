package com.shop.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Security에서 인증되지 않은 사용자가 보호된 리소스에 접근할 때 (401 Unauthorized)
 * 호출되어 JSON 형태의 응답을 반환하는 역할.
 * JWT 토큰이 없거나, 만료되었거나, 유효하지 않은 경우 주로 사용됨.
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.warn("인증되지 않은 사용자 접근 (401 Unauthorized): {}", authException.getMessage());

        // HTTP 401 Unauthorized 응답 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("utf-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // React에서 처리하기 쉽도록 JSON Body 구성
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorDetails.put("error", "Unauthorized");
        // request.getAttribute()를 사용하여 JwtAuthenticationFilter에서 설정한 에러 메시지를 가져옴
        String message = (String) request.getAttribute("exception");
        errorDetails.put("message", message != null ? message : "인증에 실패하였습니다. 유효한 JWT 토큰이 필요합니다.");
        errorDetails.put("path", request.getRequestURI());

        // JSON 응답 작성
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(errorDetails));
    }
}
