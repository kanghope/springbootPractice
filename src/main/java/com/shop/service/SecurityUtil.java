package com.shop.service;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import com.shop.config.CustomUserDetails; // 🚨 CustomUserDetails 임포트
public class SecurityUtil {

    // Member ID(PK)를 가져오려면 이전에 사용되던 로직을 사용합니다.
    public static Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // ID를 반환할 수도 있습니다.
        // JWT의 subject가 ID라면, Long.valueOf(authentication.getName()); 을 사용
        return Long.valueOf(authentication.getName()); // subject가 ID라고 가정
    }

    /**
     * 현재 로그인한 사용자의 ID (Username)를 반환합니다.
     */
    /*
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            // 인증 정보가 없거나 익명 사용자일 경우 예외 처리 또는 기본값 반환
            // 예: throw new IllegalStateException("인증된 사용자 정보가 없습니다.");
            return "SYSTEM"; // 또는 비로그인 시 기본값
        }

        // 익명 사용자는 Authentication.getPrincipal()이 "anonymousUser" 문자열을 반환합니다.
        if ("anonymousUser".equals(authentication.getPrincipal())) {
            return "SYSTEM"; // 익명 사용자일 경우도 'SYSTEM'으로 처리
        }

        Object principal = authentication.getPrincipal();

        // 2. CustomUserDetails 타입인지 확인하고 getCreatedBy()를 호출
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getCreatedBy(); // ⭐ 이 메서드를 사용합니다.
        }

        // 3. 그 외의 경우 (대응이 필요하다면 기존 로직 유지)
        else {
            return authentication.getName();
        }
    }*/
    /**
     * ⭐️ CREATED_BY/MODIFIED_BY 필드에 저장할 사용자 '이름(Name)'을 가져옵니다.
     * @return String 사용자의 이름
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            // ⭐️ CustomUserDetails에서 특별히 정의한 '이름' Getter를 사용합니다.
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            return customUserDetails.getCreatedBy();
        }

        // 인증되지 않았거나, 토큰 인증 등 다른 방식으로 주입된 경우 (기존 Principal 값 반환)
        if (authentication != null) {
            return authentication.getName();
        }
        throw new IllegalStateException("현재 Security Context에 인증 정보가 없습니다.");
    }
}
