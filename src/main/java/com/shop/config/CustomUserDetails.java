package com.shop.config;

import com.shop.entity.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Collections;


public class CustomUserDetails extends User {
    // Member 엔티티에서 CREATED_BY로 사용할 이름 정보를 저장하는 필드
    private final String name;
    private final Long memberId;
    private final String Email;

    /**
     * Member 객체를 받아 Spring Security 인증 정보 및 추가 이름 정보를 설정합니다.
     */
    // ⭐️ JWT 인증을 위한 새로운 생성자 추가 ⭐️

    public CustomUserDetails(String memberIdString, String name, String memberEmail, Collection<? extends GrantedAuthority> authorities) {
        super(
                memberIdString, // Principal 값 (ID)
                "",             // JWT 인증이므로 비밀번호는 빈 문자열
                authorities
        );
        this.Email = memberEmail;//이메일정보 저장
        this.memberId = Long.valueOf(memberIdString);
        this.name = name; // 사용자 이름 저장
    }

    public CustomUserDetails(Member member) {
        // 1. 부모(User) 클래스 생성자 호출: 이메일, 암호, 권한 설정
        super(
                member.getEmail(), // username (인증 식별자)
                member.getPassword(),
                createAuthorities(member.getRole().toString())
        );

        // 2. 추가 필드 초기화: Member 엔티티의 이름을 저장
        this.Email = member.getEmail();
        this.name = member.getName();
        this.memberId = member.getId();
    }


    /**
     * ItemService의 CREATED_BY/MODIFIED_BY 필드에 설정할 '이름'을 반환합니다.
     * 이 메서드가 기존 Item 엔티티의 Getter 이름과 유사하게 동작합니다.
     */
    public String getCreatedBy() {
        return this.name;
    }


    // 이외의 다른 필드(memberId, email 등)를 가져오는 Getter는 모두 생략했습니다.

    /**
     * 문자열 형태의 Role을 GrantedAuthority 객체 목록으로 변환하는 헬퍼 메서드
     */
    private static Collection<? extends GrantedAuthority> createAuthorities(String role) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    public Long getMemberId() {
        return this.memberId;
    }

    public String getEmail() {
        return this.Email;
    }
}
