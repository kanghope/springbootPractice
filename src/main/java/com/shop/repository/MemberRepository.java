package com.shop.repository;

import com.shop.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// JpaRepository 상속 제거 및 @Mapper 어노테이션 추가
@Mapper
public interface MemberRepository {
    // 1. 회원 저장: JpaRepository의 save(Member) 대체
    // MyBatis는 이 메서드 호출 시 MemberMapper.xml의 <insert id="save">를 실행합니다.
    void save(Member member);

    // 2. 이메일로 회원 조회: JpaRepository의 findByEmail(String) 대체
    // MyBatis는 이 메서드 호출 시 MemberMapper.xml의 <select id="findByEmail">를 실행합니다.
    // 매개변수에 @Param을 붙여 XML에서 #{email}로 접근 가능하게 합니다.
    Member findByEmail(@Param("email") String email);

    // *UserDetailsService 구현을 위해 ID로 조회하는 메서드는 필요 없지만,
    // 일반적으로 사용되므로 findById를 추가할 수 있습니다.
    Member findById(@Param("id") Long id);

    // ⭐ 카카오 ID로 회원 조회 메서드 추가
    Member findByKakaoId(@Param("kakaoId") String kakaoId);
}