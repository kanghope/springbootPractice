package com.shop.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 표준화된 에러 응답 구조를 위한 DTO
 */
@Getter
@Builder
public class ErrorResponse {
    // HTTP 상태 코드 (e.g., 401, 404)
    private final int status;

    // 에러 발생 시각
    private final LocalDateTime timestamp;

    // 개발자를 위한 에러 유형 (e.g., BadCredentials)
    private final String error;

    // 사용자에게 보여줄 메시지 (AuthService에서 던진 메시지)
    private final String message;

    // 요청 URI 경로
    private final String path;
}
