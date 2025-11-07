// JwtTokenDto.java
package com.shop.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder // Lombok의 Builder 패턴을 사용하여 쉽게 객체를 생성합니다.
public class JwtTokenDto {
    private String accessToken;
    private String refreshToken;
    private String userId; // 회원 ID (Long이지만 String으로 처리하면 편리)
    private String role;   // UserRole (예: "ROLE_USER")
    // private Integer accessTokenExpiresIn; // 필요에 따라 추가
}