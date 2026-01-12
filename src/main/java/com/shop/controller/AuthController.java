package com.shop.controller;

import com.shop.dto.KakaoTokenDto;
import com.shop.dto.KakaoUserInfo;
import com.shop.dto.TokenResponseDto;
import com.shop.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import com.shop.service.KakaoOauthService;
import org.springframework.beans.factory.annotation.Value; // ⭐️ @Value를 위한 import
import jakarta.servlet.http.HttpServletRequest; // 👈 필요시
import jakarta.servlet.http.HttpServletResponse; // 👈 필수
import jakarta.servlet.http.Cookie; // 👈 쿠키를 직접 설정/제거할 경우

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final KakaoOauthService kakaoOauthService;
    private final AuthService authService;

    // ⭐️ 에러 발생 시 리다이렉트할 프론트엔드 로그인 페이지 URL 설정 (Query Parameter로 에러 전달)
    @Value("${front.redirect-url.login:http://localhost:3000/members/login}")
    private String frontendLoginUrl;

    // ⭐️ [추가] 에러 발생 시 리다이렉트할 프론트엔드 회원가입 페이지 URL 설정
    @Value("${front.redirect-url.join:http://localhost:3000/members/new}")
    private String frontendJoinUrl;

    // React 앱의 소셜 로그인 콜백 처리 페이지
    private final String REACT_SOCIAL_CALLBACK_URL = "http://localhost:3000/auth/social/callback";

    // ⭐️ MemberController에 있는 보조 메서드 재사용을 위해 MemberController의 의존성을 주입하거나,
    // ⭐️ 이 메서드를 AuthController에 복사/붙여넣기 해야 합니다.
    private void addTokenToCookie(HttpServletResponse response, String name, String value, long maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        // cookie.setSecure(true); // 운영 환경(HTTPS)에서는 필수
        cookie.setMaxAge((int) maxAge);
        response.addCookie(cookie);
    }
    /**
     * 카카오 로그인 콜백 처리
     * 1. 카카오에서 인가 코드를 받아옴
     * 2. AuthService에 인가 코드를 넘겨 카카오 로그인/회원가입 처리
     * 3. AuthService로부터 받은 서비스 JWT(TokenResponseDto)를 쿼리 파라미터에 담아
     * React 앱의 콜백 페이지로 리디렉션
     */
    @GetMapping("/kakao/callback")
    public ResponseEntity<Void> kakaoCallback(@RequestParam String code, @RequestParam(required = false) String state,
                                              // ⭐️ HttpServletResponse 인자 추가
                                          HttpServletResponse response) {
        try {
            // 1 & 2. 인가 코드로 로그인/회원가입 처리 및 서비스 토큰 발급
            TokenResponseDto tokenResponse = authService.processKakaoLogin(code);

            // ⭐️ [핵심 수정] 쿼리 파라미터 대신 쿠키로 토큰 전달
            // Access Token
            addTokenToCookie(response, "accessToken", tokenResponse.getAccessToken(), tokenResponse.getAccessTokenExpiresIn() / 1000L);
            // Refresh Token
            addTokenToCookie(response, "refreshToken", tokenResponse.getRefreshToken(), 60 * 60 * 24 * 7L); // 예: 7일 (초)

            // 응답 본문에서 민감한 토큰 정보를 제거 (클라이언트에서 JS로 읽을 필요가 없는 정보)
            tokenResponse.setAccessToken(null);
            tokenResponse.setRefreshToken(null);
            tokenResponse.setGrantType(null);
            tokenResponse.setAccessTokenExpiresIn(null);

            // 리다이렉트 URL 생성: 토큰 제외, 사용자 역할/ID 등 JS에서 필요한 정보만 전달
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(REACT_SOCIAL_CALLBACK_URL)
                    .queryParam("userId", tokenResponse.getId())
                    .queryParam("role", tokenResponse.getRole());
           /* // 3. React 앱으로 리디렉션
            // 발급된 토큰과 사용자 정보를 쿼리 파라미터로 추가
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(REACT_SOCIAL_CALLBACK_URL)
                    .queryParam("accessToken", tokenResponse.getAccessToken())
                    .queryParam("refreshToken", tokenResponse.getRefreshToken())
                    .queryParam("userId", tokenResponse.getId()) // ⭐️ AuthService에서 id도 반환하도록 수정 필요
                    .queryParam("role", tokenResponse.getRole())
                    .queryParam("accessTokenExpiresIn", tokenResponse.getAccessTokenExpiresIn());
*/
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(uriBuilder.build().toUri());

            // 302 Found 리디렉션
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }
        catch (IllegalStateException e) {
            // ⭐️ IllegalStateException 발생 시 (예: 중복 이메일)
            String errorMessage = e.getMessage();

            String targetUrl = frontendLoginUrl;
            //state가 join인 경우 회원가입 페이지로 에러를 리다이렉트합니다.
            if("join".equals(state))
            {
                targetUrl = frontendJoinUrl;
            }

            // 에러 메시지를 포함하여 프론트엔드 로그인 페이지로 리다이렉트 (Query Parameter 사용)
            String redirectUrl = UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("error", errorMessage)
                    .build().toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(UriComponentsBuilder.fromUriString(redirectUrl).build().toUri());

            // 302 Found 리디렉션
            return new ResponseEntity<>(headers, HttpStatus.FOUND);

        } catch (Exception e) {
            // 기타 모든 예외 (500) 처리
            String errorMessage = "소셜 로그인 중 서버 내부 오류가 발생했습니다. (" + e.getMessage() + ")";

            // 오류 메시지를 포함하여 프론트엔드 로그인 페이지로 리다이렉트
            String redirectUrl = UriComponentsBuilder.fromUriString(frontendLoginUrl)
                    .queryParam("error", errorMessage)
                    .build().toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(UriComponentsBuilder.fromUriString(redirectUrl).build().toUri());

            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }
    }

    /*
    @GetMapping("/kakao/callback")
    public RedirectView kakaoLoginCallback(@RequestParam String code) {
        // 1. 인가 코드로 카카오 토큰 받기 (Service 계층 호출)
        KakaoTokenDto tokenDto = kakaoOauthService.getKakaoToken(code);

        // 2. 토큰으로 사용자 정보 받기
        KakaoUserInfo userInfo = kakaoOauthService.getKakaoUserInfo(tokenDto.getAccessToken());

        // 3. 사용자 처리 및 JWT 생성 (가정)
        // 이 로직은 DB 저장, 로그인 처리 등을 포함하며 최종적으로 JWT 정보를 반환합니다.
        JwtTokenDto jwtDto = authService.processKakaoLogin(userInfo);

        // 4. ⭐ 핵심: React 앱으로 최종 리다이렉션 ⭐
        String reactAppBaseUrl = "http://localhost:3000"; // 👈 React 앱의 기본 URL (포트 확인 필요!)
        String redirectPath = "/auth/social/callback"; // 👈 React 라우트 경로 (/App.tsx에서 정의됨)

        // JWT 정보를 쿼리 파라미터에 담아 Redirect URL 생성
        String finalRedirectUrl = reactAppBaseUrl + redirectPath +
                "?accessToken=" + jwtDto.getAccessToken() +
                "&refreshToken=" + jwtDto.getRefreshToken() +
                "&userId=" + jwtDto.getUserId() +
                "&role=" + jwtDto.getRole();

        // RedirectView를 사용하여 클라이언트 브라우저를 최종 URL로 이동시킵니다.
        return new RedirectView(finalRedirectUrl);
    }*/

}