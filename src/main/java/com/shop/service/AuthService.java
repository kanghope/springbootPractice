package com.shop.service;

import com.shop.jwt.JwtTokenProvider;
import com.shop.dto.LoginDto;
import com.shop.dto.TokenResponseDto;
import com.shop.entity.Member;
import com.shop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder; // Spring Security 가정
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.BadCredentialsException; // ⭐ 추가: Spring Security 예외
import java.util.NoSuchElementException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

// ⭐ 신규 import
import com.shop.dto.KakaoTokenDto;
import com.shop.dto.KakaoUserInfo;
import java.util.UUID;
import com.shop.constant.Role;


@Service
//@RequiredArgsConstructor

public class AuthService {

    private final MemberRepository memberRepository; // MyBatis Repository
    private final JwtTokenProvider tokenProvider;  // JWT 토큰 생성 전담 (가정된 클래스)
    private final PasswordEncoder passwordEncoder;  // 비밀번호 암호화/비교
    private final RefreshTokenService refreshTokenServic;// ⭐ 주입 추가
    private final KakaoOauthService kakaoOauthService; // ⭐️ 카카오 서비스 주입

    public AuthService(MemberRepository memberRepository, JwtTokenProvider tokenProvider, PasswordEncoder passwordEncoder
    , RefreshTokenService refreshTokenServic, KakaoOauthService kakaoOauthService)
    {
        this.memberRepository = memberRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenServic = refreshTokenServic;
        this.kakaoOauthService = kakaoOauthService;
    }



    /**
     * 1. 표준 이메일/비밀번호 로그인 처리
     * - 이메일로 회원을 조회하고, 비밀번호 일치 여부를 검증합니다.
     *
     * @param loginDto 로그인 요청 DTO (email, password)
     * @return TokenResponseDto
     */
    public TokenResponseDto login(LoginDto loginDto) {

        // 1. 이메일로 사용자 조회 (MyBatis findByEmail 쿼리 사용)
        Member member = memberRepository.findByEmail(loginDto.getEmail());

        if (member == null) {
            // ⭐ 인증 실패 시 Spring Security 표준 예외 (BadCredentialsException) 사용
            // 이 예외는 AuthenticationException을 상속하므로 Spring Security가 401로 처리할 수 있습니다.
            throw new BadCredentialsException("해당 이메일로 등록된 회원이 없습니다.");
        }

        // 2. 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(loginDto.getPassword(), member.getPassword())) {
            // ⭐ 비밀번호 불일치 시 BadCredentialsException 사용
            throw new BadCredentialsException("해당 이메일로 등록된 회원이 없거나 비밀번호가 일치하지 않습니다.");
        }

        // 3. 인증 성공 후 JWT 토큰 생성 및 반환
        TokenResponseDto tokenResponse = tokenProvider.generateTokenDto(member);

        return tokenResponse;
    }

    /**
     * 2. 카카오 ID를 사용한 소셜 로그인 처리
     * - 카카오 ID로 회원을 조회하고, 즉시 토큰을 발급합니다.
     *
     * @param kakaoId 카카오 고유 ID
     * @return TokenResponseDto
     */
    public TokenResponseDto kakaoLogin(String kakaoId) {

        Member member = memberRepository.findByKakaoId(kakaoId);

        if (member == null) {
            // 이 메서드가 호출되는 시점(processKakaoLogin)에는 회원이 보장되어야 함
            throw new BadCredentialsException("카카오 ID로 회원을 찾는 데 실패했습니다. (내부 오류)");
        }

        // 2. 소셜 로그인은 이미 외부(카카오)에서 인증이 완료되었으므로, 바로 토큰 생성
        TokenResponseDto tokenResponse = tokenProvider.generateTokenDto(member);

        // ⭐️ AuthController에서 리디렉션 시 id가 필요하므로 Dto에 id를 추가합니다.
        // ⭐️ (주의) tokenProvider.generateTokenDto가 id를 반환하도록 수정하거나,
        // ⭐️ 여기서 DTO를 다시 빌드해야 합니다.
        // ⭐️ tokenProvider.generateTokenDto가 id를 반환한다고 가정합니다.
        // ⭐️ 만약 그렇지 않다면, TokenResponseDto 빌더를 여기서 직접 사용하세요.
        /* // 예시: tokenProvider.generateTokenDto가 id를 반환하지 않을 경우
        return TokenResponseDto.builder()
                .grantType(tokenResponse.getGrantType())
                .accessToken(tokenResponse.getAccessToken())
                .accessTokenExpiresIn(tokenResponse.getAccessTokenExpiresIn())
                .refreshToken(tokenResponse.getRefreshToken())
                .id(member.getId()) // ⭐️ 사용자 ID 추가
                .email(member.getEmail())
                .name(member.getName())
                .role(member.getRole().toString())
                .build();
        */

        // ⭐️ 일단 기존 메서드가 id를 포함한 모든 정보를 반환한다고 가정합니다.
        return tokenResponse;
    }
    /**
     * ⭐️ [신규] 카카오 인가 코드를 이용한 로그인/회원가입 처리
     * * @param code 카카오가 발급한 인가 코드
     * @return TokenResponseDto (서비스 자체 JWT)
     */
    @Transactional
    public TokenResponseDto processKakaoLogin(String code) {
        // 1. 인가 코드로 카카오 토큰 받기
        KakaoTokenDto kakaoToken = kakaoOauthService.getKakaoToken(code);

        // 2. 카카오 토큰으로 사용자 정보 받기
        KakaoUserInfo userInfo = kakaoOauthService.getKakaoUserInfo(kakaoToken.getAccessToken());

        // 3. 카카오 ID로 회원 조회
        String kakaoId = userInfo.getId().toString();
        Member member = memberRepository.findByKakaoId(kakaoId);

        // 4. 회원이 없으면? -> 신규 회원 가입 (소셜)
        if (member == null) {
            String email = userInfo.getKakaoAccount().getEmail();
            String name = userInfo.getKakaoAccount().getProfile().getNickname();

            // (선택) 만약 이메일이 중복되면 기존 계정에 연동하는 로직도 가능
            Member findMember = memberRepository.findByEmail(email);
            if(findMember != null){
                throw new IllegalStateException("이미 가입 되어있는 이메일 입니다.");
            }

            Member newMember = Member.builder()
                    .email(email) // 카카오 제공 이메일
                    .name(name)   // 카카오 제공 닉네임
                    .password(passwordEncoder.encode(UUID.randomUUID().toString())) // 소셜 로그인은 비밀번호가 없으므로 임의의 값 사용
                    //.role(Role.USER) // ⭐️ Role Enum 사용 (없다면 "ROLE_USER" 문자열)
                    .role(Role.ADMIN)
                    .kakaoId(kakaoId)
                    // .address(null) // 주소는 비워둠
                    .build();

            memberRepository.save(newMember);
            member = newMember; // 새로 저장된 멤버를 사용
        }

        // 5. (기존 또는 신규) 회원에 대해 서비스 JWT 발급
        // ⭐️ [수정] id, name, email 등 모든 정보가 포함된 DTO를 반환하도록 수정
        TokenResponseDto serviceTokenDto = tokenProvider.generateTokenDto(member);

        // ⭐️ AuthController에서 리디렉션 시 ID가 필요합니다.
        // ⭐️ generateTokenDto가 ID를 반환하지 않는 경우, 여기서 직접 빌드합니다.
        return TokenResponseDto.builder()
                .grantType(serviceTokenDto.getGrantType())
                .accessToken(serviceTokenDto.getAccessToken())
                .accessTokenExpiresIn(serviceTokenDto.getAccessTokenExpiresIn())
                .refreshToken(serviceTokenDto.getRefreshToken())
                .id(member.getId()) // ⭐️ 사용자 ID (PK) 추가
                .email(member.getEmail())
                .name(member.getName())
                .role(member.getRole().toString())
                .build();
    }

    /**
     * Access Token 재발급 메서드
     * @param refreshToken 클라이언트가 보낸 Refresh Token
     * @return 새로운 Access Token과 사용자 정보가 담긴 TokenResponseDto
     */

    @Transactional
    public TokenResponseDto refresh(String refreshToken) {
        // 1. Refresh Token 유효성 검증 (JWT 형식/서명/만료 검증)
        try {
            if (!tokenProvider.validateToken(refreshToken)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Refresh Token structure.");
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token is expired. Please log in again.");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Refresh Token.");
        }
        // 2. Refresh Token에서 Member ID(subject) 추출
        Authentication authentication = tokenProvider.getAuthentication(refreshToken);
        Long memberId = Long.valueOf(authentication.getName()); // subject: Member ID (Long)

        // 3. 서버 저장소(DB)의 Refresh Token과 비교 및 유효성 확인
        // isRefreshTokenValid 메서드를 Long 타입을 받도록 수정했습니다.
        if (!refreshTokenServic.isRefreshTokenValid(memberId, refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token mismatch or expired. Please log in again.");
        }

        // 4. 사용자 정보 조회
        Member member = memberRepository.findById(memberId);

        if(member == null)
        {
            // Member를 찾지 못한 경우 직접 예외 발생
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        }


        // 5. 새로운 Access Token 생성
        String newAccessToken = tokenProvider.createAccessToken(member);
        long newAccessTokenExpiresIn = tokenProvider.getAccessTokenExpireTime();

        // 6. TokenResponseDto 빌드 및 반환
        return TokenResponseDto.builder()
                .grantType("Bearer")
                .accessToken(newAccessToken)
                .accessTokenExpiresIn(newAccessTokenExpiresIn)
                .refreshToken(refreshToken) // 기존 Refresh Token 재사용
                // ⭐ 추가: 로그인과 마찬가지로 사용자 정보를 응답에 포함
                .email(member.getEmail())
                .name(member.getName())
                .role(member.getRole().toString())
                .build();
    }
    /**
     * (선택적) 로그아웃 시 Refresh Token을 DB/Redis에서 제거
     */

    public void deleteRefreshToken(Long memberId) {
        refreshTokenServic.deleteByMemberId(memberId);
    }

}
