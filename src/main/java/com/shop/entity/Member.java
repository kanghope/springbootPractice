package com.shop.entity;

import com.shop.constant.Role;
import com.shop.dto.MemberFormDto;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

// MyBatis 환경에서 단순 데이터 객체(VO/DTO) 역할
// JPA 관련 어노테이션(@Entity, @Table, @Id 등) 및 상속(extends BaseEntity) 제거
@Getter @Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED) // <--- 이 코드를 추가하세요.

public class Member {

    private Long id; // DB 컬럼명과 일치하는 필드 (member_id)
    private String name;
    private String email;
    private String password;
    private String address;
    private Role role; // enum 타입 유지
    // DB 테이블에 정의된 필드 추가
    private String createdBy; // 등록자
    private String modifiedBy; // 수정자
    private java.time.LocalDateTime regTime; // 등록 시간
    private java.time.LocalDateTime updateTime; // 수정 시간
    // ⭐ kakaoId 필드 Getter/Setter 추가
    private String kakaoId;
    /**
     * 회원 가입 정보를 받아 Member 객체를 생성하는 정적 팩토리 메서드.
     * 비밀번호를 암호화하고 기본 역할을 할당하는 비즈니스 로직을 포함합니다.
     * @param memberFormDto 회원 가입 DTO
     * @param passwordEncoder 비밀번호 암호화 객체
     * @return Member 객체
     */
    public static Member createMember(MemberFormDto memberFormDto, PasswordEncoder passwordEncoder){
        Member member = new Member();
        member.setName(memberFormDto.getName());
        member.setEmail(memberFormDto.getEmail());
        member.setAddress(memberFormDto.getAddress());
        member.setCreatedBy(memberFormDto.getCreatedBy());
        //member.setRegTime(memberFormDto.getRegTime());

        // 비밀번호 암호화 로직 유지
        String password = passwordEncoder.encode(memberFormDto.getPassword());
        member.setPassword(password);

        // 기본 역할(Role) 할당 로직 유지
        // 현재 코드는 무조건 ADMIN으로 설정되어 있으나, 일반적인 경우 USER로 설정해야 합니다.
        //member.setRole(Role.USER);
        member.setRole(Role.ADMIN); // 기존 코드 유지

        return member;
    }

    // ----------------------------------------------------
    // ⭐ 2. 카카오 소셜 로그인을 위한 정적 팩토리 메서드 (추가)
    // ----------------------------------------------------
    /**
     * 카카오 인증 정보를 사용하여 Member 엔티티를 생성합니다. (신규 가입용)
     */
    public static Member createSocialMember(String email, String name, String kakaoId) {
        Member member = new Member();

        // 소셜 정보 설정
        member.setName(name);
        member.setEmail(email);
        member.setKakaoId(kakaoId); // ⭐ 카카오 고유 ID 저장

        // 필수 값: 주소는 소셜 로그인 시 알 수 없으므로 임시 값 설정 (필요에 따라 DTO로 받아야 함)
        member.setAddress("소셜가입");

        // 비밀번호: Spring Security 소셜 로그인은 비밀번호가 필요 없지만,
        // DB 테이블의 NOT NULL 제약 조건을 맞추기 위해 임의의 암호화된 값 설정
        // UUID를 사용하면 충돌 위험이 적고 길이를 맞추기 쉽습니다.
        member.setPassword(UUID.randomUUID().toString());

        // 기본 역할 설정
        member.setRole(Role.USER);

        // BaseEntity 필드 설정
        member.setCreatedBy(name);

        return member;
    }
}
