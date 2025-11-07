package com.shop.service;

import com.shop.entity.Member;
import com.shop.constant.Role;
import com.shop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// ⭐ 추가된 import 구문
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.shop.config.CustomOAuth2Principal; // ⭐ CustomOAuth2Principal의 위치에 따라 패키지 수정 필요

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 카카오 서버에서 사용자 정보(attributes) 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. 카카오 사용자 정보 파싱
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String socialId = String.valueOf(attributes.get("id"));

        // 카카오 계정 정보 추출 (null 체크는 생략되었으나 실제 운영 환경에서는 필요)
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        String email = (String) kakaoAccount.get("email");

        // 카카오 프로필 정보 추출
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String nickname = (String) profile.get("nickname");

        // 3. DB에 저장 또는 업데이트
        Member member = saveOrUpdate(email, socialId, nickname);

        // Member의 Role을 기반으로 authorities를 생성합니다.
        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + member.getRole().toString())
        );

        // 4. Spring Security가 사용할 인증 객체 반환
        return new CustomOAuth2Principal(
                member.getEmail(),
                member.getPassword(),
                authorities, // DB ROLE 기반 권한
                attributes
        );
    }

    /**
     * 카카오 ID로 회원 조회 후 없으면 신규 가입, 있으면 정보 업데이트
     */
    private Member saveOrUpdate(String email, String socialId, String nickname) {
        // 기존 회원은 email이 아닌 socialId(KAKAO_ID)로 조회하는 것이 더 정확합니다.
        Member member = memberRepository.findByKakaoId(socialId);

        if (member == null) {
            // 2. KAKAO_ID로 못 찾았다면, EMAIL로 조회 (기존 일반 회원인 경우)
            member = memberRepository.findByEmail(email);

            if(member == null)
            {
                // 3.카카오아이디와 이메일 모두 없는경우 신규회원가입
                member = Member.createSocialMember(email, nickname, socialId);
                memberRepository.save(member);
            }
            else {
                //4.이메일은 존재하지만 카카오아이디가 없는경우
                // Spring Security가 처리할 수 있는 예외를 던져야 합니다.
                throw new OAuth2AuthenticationException("이미 일반 회원으로 가입된 이메일입니다. 일반 로그인 후 카카오 연동을 시도하거나, 다른 카카오 계정을 사용해주세요.");
            }


        } else {
            // 기존 회원 정보 업데이트 (필요 시 추가)
            // member.update(nickname, email);
            // memberRepository.save(member);
        }
        return member;
    }
}


