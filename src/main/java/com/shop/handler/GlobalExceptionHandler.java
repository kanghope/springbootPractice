package com.shop.handler;

import com.shop.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException; // BadCredentialsException의 상위 클래스
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException; // @Valid 실패 예외
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리: 애플리케이션 전반에서 발생하는 예외를 한 곳에서 처리하고,
 * 제공된 ErrorResponse DTO 구조에 맞게 표준화된 JSON 응답 형태로 클라이언트에게 반환합니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 1. MethodArgumentNotValidException (@Valid를 통한 DTO 유효성 검사 실패) 처리
     * HTTP Status: 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {

        // 모든 필드 에러를 모아서 하나의 메시지 문자열로 변환 (사용자에게 어떤 필드가 문제인지 알려줌)
        String detailedMessage = e.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String fieldName = (error instanceof FieldError) ? ((FieldError) error).getField() : "Global";
                    return fieldName + ": " + error.getDefaultMessage();
                })
                .collect(Collectors.joining("; "));

        log.warn("Validation Failed: {} - Path: {}", detailedMessage, request.getRequestURI());

        HttpStatus status = HttpStatus.BAD_REQUEST; // 400

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status.value())
                .timestamp(LocalDateTime.now())
                .error(status.getReasonPhrase()) // Bad Request
                .message("요청 데이터 형식이 올바르지 않습니다: " + detailedMessage)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * 2. AuthenticationException (로그인 실패: ID/PW 불일치, 토큰 만료 등) 처리
     * BadCredentialsException을 포함하는 상위 예외 처리
     * HTTP Status: 401 Unauthorized
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException e,
            HttpServletRequest request) {

        log.warn("Authentication Failed: {} - Path: {}", e.getMessage(), request.getRequestURI());

        HttpStatus status = HttpStatus.UNAUTHORIZED; // 401

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status.value())
                .timestamp(LocalDateTime.now())
                .error(status.getReasonPhrase()) // Unauthorized
                .message("인증 실패: 아이디 또는 비밀번호를 확인해주세요.") // 상세 메시지 숨김
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * 3. IllegalStateException (비즈니스 로직 오류: 중복 이메일, 상태 오류 등) 처리
     * HTTP Status: 409 Conflict
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException e,
            HttpServletRequest request) {

        log.warn("Business Logic Conflict: {} - Path: {}", e.getMessage(), request.getRequestURI());

        HttpStatus status = HttpStatus.CONFLICT; // 409

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status.value())
                .timestamp(LocalDateTime.now())
                .error(status.getReasonPhrase()) // Conflict
                .message(e.getMessage()) // 비즈니스 로직에서 던진 상세 메시지를 사용자에게 보여줍니다.
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }


    /**
     * 4. NoSuchElementException (데이터를 찾을 수 없을 때) 처리
     * HTTP Status: 404 Not Found
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(
            NoSuchElementException e,
            HttpServletRequest request) {

        log.info("Resource Not Found: {} - Path: {}", e.getMessage(), request.getRequestURI());

        HttpStatus status = HttpStatus.NOT_FOUND; // 404

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status.value())
                .timestamp(LocalDateTime.now())
                .error(status.getReasonPhrase()) // Not Found
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }


    /**
     * 5. 기타 모든 예상치 못한 예외 처리 (최후의 방어선)
     * HTTP Status: 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(
            Exception e,
            HttpServletRequest request) {

        log.error("Unhandled Server Error: {}", e.getMessage(), e);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // 500

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status.value())
                .timestamp(LocalDateTime.now())
                .error(status.getReasonPhrase()) // Internal Server Error
                .message("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.") // 내부 정보를 숨김
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }
}
