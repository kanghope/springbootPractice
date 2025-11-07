package com.shop.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TokenResponseDto {
    private String grantType;          // 토큰 타입 (예: Bearer)
    private String accessToken;        // 실제 액세스 토큰
    private Long accessTokenExpiresIn; // 액세스 토큰 만료 시간 (밀리초)
    private String refreshToken;       // 리프레시 토큰

    // ⭐ [추가] 로그인 성공 시 프론트엔드에 전달할 사용자 정보
    private String email;               // 사용자 이메일
    private String name;                // 사용자 이름
    private String role;                // 사용자 역할 (예: ROLE_USER)
    private Long id; // DB 컬럼명과 일치하는 필드 (member_id)
}