package com.shop.config; // 혹은 com.shop.service 등 적절한 패키지에 위치

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
public class CustomOAuth2Principal implements OAuth2User, UserDetails{
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Object> attributes;

    /**
     * CustomOAuth2UserService에서 호출할 생성자.
     */
    public CustomOAuth2Principal(String email, String password,
                                 Collection<? extends GrantedAuthority> authorities,
                                 Map<String, Object> attributes) {
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.attributes = attributes;
    }
    // ----------------------------------------------------
    // ⭐ UserDetails 구현 메서드 (일반 로그인 필드 제공)
    // ----------------------------------------------------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        // DB에 저장된 암호화된 비밀번호 (소셜 회원은 UUID 값)
        return this.password;
    }

    @Override
    public String getUsername() {
        // UserDetails에서 식별자로 사용될 이메일
        return this.email;
    }

    // 이하는 기본적으로 true 반환
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // ----------------------------------------------------
    // ⭐ OAuth2User 구현 메서드 (소셜 로그인 필드 제공)
    // ----------------------------------------------------

    @Override
    public Map<String, Object> getAttributes() {
        // 소셜 인증 서버(카카오)에서 받은 원본 데이터
        return attributes;
    }

    @Override
    public String getName() {
        // OAuth2User의 주 식별자. 카카오의 경우 'id' 필드를 사용합니다.
        // application.yml에서 user-name-attribute: id 로 설정했으므로,
        // 해당 필드를 반환해야 합니다.
        return String.valueOf(attributes.get("id"));
    }
}
