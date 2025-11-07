package com.shop.repository; // 일반적으로 Repository나 Mapper 패키지에 위치

import com.shop.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RefreshTokenMapper {
    // 1. 토큰 저장/업데이트 (Upsert)
    // - 사용자 ID가 이미 존재하면 UPDATE, 없으면 INSERT (Merge 또는 조건부 INSERT/UPDATE)
    void save(RefreshToken refreshToken);

    // 2. 토큰 조회
    RefreshToken findByMemberId(Long memberId);

    // 3. 토큰 삭제 (로그아웃, 탈퇴 등)
    int deleteByMemberId(Long memberId);

    // (선택적) 4. 토큰 유효성 검증 (클라이언트 토큰과 저장된 토큰 비교)
    RefreshToken findByMemberIdAndToken(Long memberId, String refreshToken);
}
