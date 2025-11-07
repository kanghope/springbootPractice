package com.shop.service;

import com.shop.entity.Member;
import com.shop.repository.MemberRepository; // MyBatis Mapper를 주입받음
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Spring Security 관련 클래스 임포트 추가 (핵심 수정)
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.shop.config.CustomUserDetails; // 🚨 CustomUserDetails 임포트 추가

@Service
@Transactional // 트랜잭션 관리는 MyBatis에서도 유지됩니다.
@RequiredArgsConstructor
public class MemberService implements UserDetailsService{

    // MyBatis Mapper 인터페이스가 주입됩니다. (코드 변경 없음)
    private final MemberRepository memberRepository;

    public Member saveMember(Member member){
        validateDuplicateMember(member);

        // MyBatis Mapper의 save() 메서드 호출 (코드 변경 없음)
        memberRepository.save(member);
        return member;
        // 참고: JPA는 save 호출 후 영속성 컨텍스트에서 반환하지만,
        // MyBatis는 보통 void 또는 영향을 받은 행 수를 반환합니다.
        // 여기서는 편의상 Member 객체를 반환하도록 두었습니다.
    }

    private void validateDuplicateMember(Member member){
        // MyBatis Mapper의 findByEmail() 메서드 호출 (코드 변경 없음)
        Member findMember = memberRepository.findByEmail(member.getEmail());
        if(findMember != null){
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // MyBatis Mapper의 findByEmail() 메서드 호출 (코드 변경 없음)
        Member member = memberRepository.findByEmail(email);

        if(member == null){
            throw new UsernameNotFoundException(email);
        }
        // 2. 조회된 정보를 Spring Security가 사용할 User 객체로 빌드하여 반환
        // 🚨 핵심 수정: User.builder() 대신 CustomUserDetails 객체를 생성하여 반환
        return new CustomUserDetails(member);
//        return User.builder()
//                .username(member.getEmail())
//                .password(member.getPassword())
//                .roles(member.getRole().toString())
//                .build();
    }
}
