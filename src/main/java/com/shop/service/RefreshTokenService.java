package com.shop.service;

import com.shop.entity.RefreshToken;
import com.shop.repository.RefreshTokenMapper; // 매퍼 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional; // Optional 처리는 DB 조회의 안전성을 높입니다.

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenMapper refreshTokenMapper;

    /**
     * 1. Refresh Token 정보를 DB에 저장하거나 갱신합니다.
     * @param memberId 사용자 ID
     * @param token 실제 Refresh Token 문자열
     * @param expiryDate 토큰 만료 일시
     */
    public void saveOrUpdate(Long memberId, String token, Date expiryDate) {
        RefreshToken refreshToken = RefreshToken.builder()
                .memberId(memberId)
                .refreshToken(token)
                .expiryDate(expiryDate)
                .build();

        refreshTokenMapper.save(refreshToken);
    }

    /**
     * 2. 서버에 저장된 Refresh Token이 클라이언트에서 온 토큰과 일치하는지 확인합니다.
     * @param memberId 사용자 ID
     * @param clientRefreshToken 클라이언트가 보낸 토큰
     * @return 일치하고 만료되지 않았으면 true, 아니면 false
     */
    public boolean isRefreshTokenValid(Long memberId, String clientRefreshToken) {
        // DB에서 해당 사용자 ID로 저장된 토큰을 가져옵니다.
        RefreshToken storedToken = refreshTokenMapper.findByMemberId(memberId);

        if (storedToken == null) {
            // 서버에 토큰 정보 자체가 없음 (로그아웃 했거나, 만료되어 자동 삭제되었거나)
            return false;
        }

        // 1. 클라이언트 토큰과 저장된 토큰이 일치하는지 확인 (가장 중요한 보안 검증)
        if (!storedToken.getRefreshToken().equals(clientRefreshToken)) {
            // 저장된 토큰과 다름: 탈취 시도로 간주할 수 있음
            // ⭐ 보안 로직: 이 경우, storedToken을 즉시 삭제하여 모든 Refresh Token을 무효화하는 것도 고려
            return false;
        }

        // 2. 만료 시간 확인
        // Access Token 재발급은 Refresh Token이 만료되기 전에 이루어져야 합니다.
        if (storedToken.getExpiryDate().before(new Date())) {
            // DB에 저장된 토큰이 이미 만료됨
            refreshTokenMapper.deleteByMemberId(memberId); // 만료된 토큰 정리
            return false;
        }

        return true;
    }

    /**
     * 3. Refresh Token을 삭제합니다. (로그아웃 처리)
     * @param memberId 사용자 ID
     */
    public void deleteByMemberId(Long memberId) {
        refreshTokenMapper.deleteByMemberId(memberId);
    }
}