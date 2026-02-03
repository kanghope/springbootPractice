package com.shop.config;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

/**
 * [통합 로그인 사용자 객체]
 * 1. UserDetails: 일반 ID/PW 로그인 시 사용하는 인터페이스
 * 2. OAuth2User: 카카오, 구글 등 소셜 로그인 시 사용하는 인터페이스
 * 이 두 가지를 모두 구현함으로써 컨트롤러 등에서 로그인 방식과 상관없이 이 객체를 사용할 수 있습니다.
 */
@Getter
public class CustomOAuth2Principal implements OAuth2User, UserDetails {
    
    private final String email;       // 사용자 식별자 (ID 대용)
    private final String password;    // 비밀번호 (소셜 로그인 시에는 의미 없는 임의의 값 저장)
    private final Collection<? extends GrantedAuthority> authorities; // 사용자 권한 목록 (ROLE_USER 등)
    private final Map<String, Object> attributes; // 소셜 로그인 시 제공받는 전체 유저 정보 (JSON 데이터)

    /**
     * CustomOAuth2UserService 또는 UserDetailsService에서
     * 인증이 완료된 후 이 객체를 생성하여 시큐리티 세션에 저장합니다.
     */
    public CustomOAuth2Principal(String email, String password,
                                 Collection<? extends GrantedAuthority> authorities,
                                 Map<String, Object> attributes) {
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.attributes = attributes;
    }

    // ====================================================
    // ⭐ UserDetails 구현부 (일반 로그인용 표준 메서드)
    // ====================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자가 가진 권한(ROLE_USER, ROLE_ADMIN 등)을 반환합니다.
        return authorities;
    }

    @Override
    public String getPassword() {
        // 시큐리티가 인증 과정에서 비밀번호를 확인할 때 사용합니다.
        return this.password;
    }

    @Override
    public String getUsername() {
        // 시큐리티에서 사용자를 식별하는 고유한 값(로그인 ID)을 반환합니다.
        // 우리 시스템에서는 이메일을 ID로 사용합니다.
        return this.email;
    }

    /* 계정 상태 관리 메서드 (보통은 모두 true로 설정하여 활성화함) */

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부 (true: 만료 안됨)
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금 여부 (true: 잠기지 않음)
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호 만료 여부 (true: 만료 안됨)
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정 활성화 여부 (true: 사용 가능)
    }

    // ====================================================
    // ⭐ OAuth2User 구현부 (소셜 로그인용 추가 메서드)
    // ====================================================

    @Override
    public Map<String, Object> getAttributes() {
        // 소셜 서버(카카오 등)로부터 받은 유저 정보 전체를 Map 형태로 반환합니다.
        return attributes;
    }

    @Override
    public String getName() {
        // OAuth2 인증의 '주 식별자'를 반환합니다.
        // 카카오 설정(user-name-attribute: id)에 따라 카카오 고유 ID 번호를 반환하도록 설정됨.
        return String.valueOf(attributes.get("id"));
    }
}
