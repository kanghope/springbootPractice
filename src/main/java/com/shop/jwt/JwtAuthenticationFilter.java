package com.shop.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.filters.ExpiresFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken"; // 👈 쿠키 이름 정의

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 토큰 추출 로직 수정: 헤더 추출 로직 제거 (또는 주석 처리)
        // String jwt = resolveToken(request); // ❌ 헤더 추출 로직 (주석 처리 또는 제거)
        String jwt = resolveTokenFromCookie(request);//✅ 쿠키에서 토큰 추출

        try {
            // 2. 토큰 유효성 검증 및 SecurityContext에 인증 정보 저장
            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
                // 토큰이 유효할 경우, 토큰으로부터 Authentication 객체를 얻어옴
                Authentication authentication = jwtTokenProvider.getAuthentication(jwt);

                // SecurityContext에 Authentication 객체를 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException e) {
            // 만료된 토큰
            request.setAttribute("exception", "만료된 JWT 토큰입니다.");
            SecurityContextHolder.clearContext();
        } catch (MalformedJwtException | io.jsonwebtoken.security.SecurityException e) {
            // 잘못된 서명 또는 구조의 토큰
            request.setAttribute("exception", "유효하지 않은 JWT 토큰입니다.");
            SecurityContextHolder.clearContext();
        } catch (UnsupportedJwtException e) {
            // 지원되지 않는 토큰
            request.setAttribute("exception", "지원되지 않는 JWT 토큰 형식입니다.");
            SecurityContextHolder.clearContext();
        } catch (IllegalArgumentException e) {
            // 토큰이 잘못된 경우 (예: Empty claims)
            request.setAttribute("exception", "JWT 토큰이 잘못되었습니다.");
            SecurityContextHolder.clearContext();
        }


        // 토큰이 유효하지 않거나 예외가 발생했더라도 다음 필터 체인으로 진행
        // 결국 보호된 리소스에 접근 시 SecurityContext에 Authentication이 없으므로
        // JwtAuthenticationEntryPoint가 호출되어 401 응답을 JSON으로 반환하게 됨.
        filterChain.doFilter(request, response);
    }

    /**
     * 보조 메서드: Request 쿠키에서 Access Token 추출
     */
    private String resolveTokenFromCookie(HttpServletRequest request)
    {
        Cookie[] cookies = request.getCookies();
        if(cookies != null)
        {
            for(Cookie cookie : cookies)
            {
                // Access Token 쿠키 이름("accessToken")을 찾아 값을 반환
                if(cookie.getName().equals(ACCESS_TOKEN_COOKIE_NAME))
                {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    /**
     * 보조 메서드: Request Header에서 토큰 정보 추출
     * 헤더: Authorization: Bearer {token}
     */
    /*
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }*/

}
