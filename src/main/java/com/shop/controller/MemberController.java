package com.shop.controller;

import com.shop.dto.MemberFormDto;
import com.shop.dto.LoginDto;
import com.shop.dto.TokenResponseDto;
import com.shop.entity.Member;
import com.shop.service.MemberService;
import com.shop.service.AuthService; // 인증 로직 전담 서비스
import lombok.RequiredArgsConstructor;

// REST API 응답을 위한 필수 Import
import org.apache.catalina.filters.ExpiresFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest; // 👈 필요시
import jakarta.servlet.http.HttpServletResponse; // 👈 필수
import jakarta.servlet.http.Cookie; // 👈 쿠키를 직접 설정/제거할 경우


@RequestMapping("api/members")
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    // -------------------------------------------------------------------------
    // 1. GET /members/new (회원가입 폼 요청 - REST 환경)
    // -------------------------------------------------------------------------
    @GetMapping(value = "/new")
    public ResponseEntity<String> memberForm() {
        return ResponseEntity.ok("회원가입 엔드포인트 준비됨.");
    }

    // -------------------------------------------------------------------------
    // 2. POST /members/new (회원가입 처리)
    // -------------------------------------------------------------------------
    @PostMapping(value = "/new")
    public ResponseEntity<?> memberForm(@Valid @RequestBody MemberFormDto memberFormDto,
                                        BindingResult bindingResult) {

        // 1. 유효성 검사 실패 (400 Bad Request)
        // NOTE: MethodArgumentNotValidException은 GlobalExceptionHandler에서 처리됨
        if(bindingResult.hasErrors()){
            /*List<String> errorMessages = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList());*/
            // 필드 에러를 Map 형태로 변환: { "name": "이름은 필수입니다.", "email": "형식이 맞지 않습니다." }
            Map<String, String> filedErrors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(
                    error -> filedErrors.put(error.getField(), error.getDefaultMessage())
            );
            // GlobalExceptionHandler가 MethodArgumentNotValidException을 처리하도록 수정하려면 이 로컬 처리를 제거해야 합니다.
            // 현재 MemberController의 기존 로직을 최대한 유지하면서, BindingResult 처리는 그대로 둡니다.
            return new ResponseEntity<>(filedErrors, HttpStatus.BAD_REQUEST);
        }

        try {
            // 2. 회원 저장 로직 (기존 로직 재사용)
            Member member = Member.createMember(memberFormDto, passwordEncoder);
            memberService.saveMember(member);

        } catch (IllegalStateException e){
            // 3. 비즈니스 로직 오류 (예: 중복 이메일) (409 Conflict)
            // NOTE: IllegalStateException은 GlobalExceptionHandler에서 처리됨.
            // Local catch를 제거하여 GlobalExceptionHandler의 409 처리기로 전달하도록 수정
            throw e;
        }

        // 4. 회원가입 성공 (201 Created)
        return new ResponseEntity<>("회원가입 성공", HttpStatus.CREATED);
    }

    // -------------------------------------------------------------------------
    // 3. POST /members/login (로그인 인증 처리) - ⭐ try-catch 제거 ⭐
    // -------------------------------------------------------------------------
    @PostMapping(value = "/login")
    // 반환 타입을 TokenResponseDto로 명확하게 지정
    //HttpServletResponse 인자 추가
    public ResponseEntity<TokenResponseDto> loginMember(@Valid @RequestBody LoginDto loginDto, HttpServletResponse response)
    // 메서드 시그니처에서 throws AuthenticationException을 선언할 필요는 없습니다.
    // RuntimeException이므로 Spring이 자동으로 처리합니다.
    {
        // ⭐ AuthService를 통해 ID/PW 검증 및 JWT 토큰 발급
        // 예외 (AuthenticationException 등) 발생 시 GlobalExceptionHandler로 자동 전파됩니다.
        // AuthService는 토큰을 생성하고, DTO를 반환합니다.
        TokenResponseDto tokenResponse = authService.login(loginDto);
        // 2. 토큰을 응답 본문 대신 HttpOnly 쿠키에 설정합니다.
        // Access Token
        addTokenToCookie(response, "accessToken", tokenResponse.getAccessToken(), tokenResponse.getAccessTokenExpiresIn() / 1000L); // 밀리초를 초로 변환
        // Refresh Token
        addTokenToCookie(response, "refreshToken", tokenResponse.getRefreshToken(), 60 * 60 * 24 * 7L); // 예: 7일 (초)

        // 응답 본문에서 민감한 토큰 정보를 제거
        tokenResponse.setAccessToken(null);
        tokenResponse.setRefreshToken(null);
        tokenResponse.setGrantType(null);
        tokenResponse.setAccessTokenExpiresIn(null);

        // 200 OK와 함께 토큰 정보 반환
        return ResponseEntity.ok(tokenResponse);
    }

    /**
     * 보조 메서드: 토큰을 HttpOnly 쿠키에 추가합니다.
     */
    private void addTokenToCookie(HttpServletResponse response, String name, String value, long maxAge)
    {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);// ⭐️ HttpOnly 설정 (JS 접근 방지)
        // cookie.setSecure(true); // 운영 환경(HTTPS)에서는 이 설정이 필수입니다.
        cookie.setMaxAge((int) maxAge);
        response.addCookie(cookie);
    }
/**
   * 보조 메서드: 요청에서 특정 이름의 쿠키 값을 추출합니다.
   */
  private String extractTokenFromCookie(HttpServletRequest request, String name)
  {
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if (cookie.getName().equals(name)) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }
    /**
       * 보조 메서드: Access/Refresh Token을 쿠키에 추가합니다.
       */
  private void addTokensToCookies(HttpServletResponse response, TokenResponseDto tokenResponse) {
    // Access Token (짧은 만료 시간)
    addTokenToCookie(response, "accessToken", tokenResponse.getAccessToken(), tokenResponse.getAccessTokenExpiresIn() / 1000L); 
    // Refresh Token (긴 만료 시간)
    addTokenToCookie(response, "refreshToken", tokenResponse.getRefreshToken(), 60 * 60 * 24 * 7L); // 7일 (예시)
  }
    /**
     * 보조 메서드: 응답에 쿠키를 제거하는 명령을 추가합니다. (기존 로직 유지)
     */
    private void removeCookie(HttpServletResponse response, String cookieName)
    {
        Cookie cookie = new Cookie(cookieName, null); // 값은 null
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    // -------------------------------------------------------------------------
    // 4. GET /members/login/error (로그인 오류 처리 - REST 환경에서는 잘 사용되지 않음)
    // -------------------------------------------------------------------------
    @GetMapping(value = "/login/error")
    public ResponseEntity<String> loginError() {
        // 이 엔드포인트는 주로 폼 기반 인증 실패 시 리다이렉션으로 사용되므로, REST 환경에서는 보조적입니다.
        return new ResponseEntity<>("로그인 실패: 잘못된 인증 요청입니다.", HttpStatus.UNAUTHORIZED);
    }

    /**
     * 로그아웃 처리 API: 클라이언트의 요청을 받아 Access/Refresh Token을 무효화하고 쿠키를 제거합니다.
     * @param request HttpServletRequest (쿠키 정보를 읽기 위해 필요)
     * @param response HttpServletResponse (쿠키를 제거하기 위해 필요)
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {

        // 1. JWT 토큰 무효화 (선택 사항):
        // JWT는 Stateless하므로 서버에서 세션을 무효화할 수는 없지만,
        // 블랙리스트(Redis 등)에 Access Token을 등록하여 남은 유효 기간 동안 무효화할 수 있습니다.
        // 여기서는 쿠키 제거에만 집중합니다.

        // 2. Access Token 쿠키 제거
        // 쿠키의 이름과 동일한 쿠키를 'max-age=0'으로 설정하여 응답합니다.
        // 서버 측에서 HttpOnly 쿠키의 이름(예: 'accessToken')을 알아야 합니다.
        removeCookie(response, "accessToken");

        // 3. Refresh Token 쿠키 제거 (Refresh Token도 HttpOnly 쿠키로 관리된다고 가정)
        removeCookie(response, "refreshToken");

        // 4. 성공 응답 반환
        return ResponseEntity.ok().build();
    }

    // -------------------------------------------------------------------------
    // 5. POST /members/refresh (Access Token 갱신 처리) - ⭐ 새로 추가된 핵심 로직 ⭐
    // -------------------------------------------------------------------------
    @PostMapping("/refresh")
    // 클라이언트에서 요청 본문 없이 HttpOnly 쿠키에 담긴 Refresh Token을 기대합니다.
    public ResponseEntity<TokenResponseDto> refreshAccessToken(
        HttpServletRequest request,
        HttpServletResponse response)    {
        // 1. HttpOnly 쿠키에서 Refresh Token 추출
        String refreshToken = extractTokenFromCookie(request, "refreshToken");

        // Refresh Token이 없거나 유효하지 않으면 401 Unauthorized 발생
        if (refreshToken == null) {
        // 프론트엔드가 이를 감지하고 강제 로그아웃 처리할 수 있도록 401 반환
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // 2. AuthService를 통해 토큰 갱신 로직 실행
        // AuthService에서 Refresh Token 유효성 검사 및 새로운 Access/Refresh Token 발급
        TokenResponseDto tokenResponse = authService.refresh(refreshToken);

        // 3. 새 토큰을 HttpOnly 쿠키에 설정 (기존 쿠키 덮어쓰기)
        addTokensToCookies(response, tokenResponse);

        // 4. 응답 본문에서 민감한 토큰 정보를 제거하고 역할(role)만 반환
        tokenResponse.setAccessToken(null);
        tokenResponse.setRefreshToken(null);
        tokenResponse.setGrantType(null);
        tokenResponse.setAccessTokenExpiresIn(null);

        // 200 OK와 함께 역할 정보 반환 (프론트엔드에서 userRole 갱신용)
        return ResponseEntity.ok(tokenResponse);
    }


}
