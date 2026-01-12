package com.shop.jwt;

import com.shop.dto.TokenResponseDto;
import com.shop.entity.Member;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders; // 이 패키지를 사용하므로 반드시 있어야 합니다.
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30; // 30분

    // (Refresh Token 만료 시간은 AuthService에서 설정하거나 DB에서 관리하는 것이 일반적입니다.)
    private final Key key;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 1. Access Token 및 Refresh Token 생성
     */
    public TokenResponseDto generateTokenDto(Member member) {
        // ... (기존 구현 코드 유지 - 토큰 생성)
        String authorities = member.getRole().toString();
        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);

        String accessToken = Jwts.builder()
                .setSubject(String.valueOf(member.getId()))
                .claim(AUTHORITIES_KEY, authorities)
                .claim("name", member.getName())
                .claim("email", member.getEmail())
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        String refreshToken = "dummy-refresh-token-" + member.getId(); // 더미 리프레시 토큰

        return TokenResponseDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .accessTokenExpiresIn(ACCESS_TOKEN_EXPIRE_TIME)
                .refreshToken(refreshToken)
                // ----------------------------------------------------
                // Member 엔티티에서 사용자 정보를 추출하여 DTO에 포함
                .email(member.getEmail())
                .name(member.getName())
                .role(member.getRole().toString())
                .id(member.getId())
                .build();
    }

    /**
     * 2. 토큰에서 인증 정보(Authentication) 추출
     * - SecurityContext에 저장할 UsernamePasswordAuthenticationToken을 만듭니다.
     */
    public Authentication getAuthentication(String accessToken) {
        // 토큰 복호화 및 클레임 추출
        Claims claims = parseClaims(accessToken);

        if (claims.get(AUTHORITIES_KEY) == null) {
            // 토큰 생성 로직이 정확하다면 발생하지 않아야 함
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 클레임에서 권한 정보 가져오기 (예: ROLE_USER)
        // 토큰 생성 시 쉼표(,)로 구분하지 않았으므로, split(",")을 사용하지 않고 직접 사용
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(",")) //ROLE_USER 형식일 경우
                        .map(role -> "ROLE_" + role.trim())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        String memberIdString = claims.getSubject();
        String memberName = claims.get("name", String.class);//⭐️ name 클레임 추출
        String memberEmail = claims.get("email", String.class);//
        // UserDetails 객체를 만들어서 Authentication 리턴
        //UserDetails principal = new User(memberIdString, memberName, authorities);
        // ⭐️ [수정] 기본 User 대신 CustomUserDetails 사용 ⭐️
        // (CustomUserDetails에 memberIdString, memberName, authorities를 받는 생성자가 있다고 가정)
        UserDetails principal = new com.shop.config.CustomUserDetails(
                memberIdString,
                memberName,
                memberEmail,
                authorities
        );
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    /**
     * 3. 토큰 유효성 검증
     * - Jwts.parserBuilder()를 사용하여 토큰을 검증합니다.
     * - 이 메서드는 이제 예외 발생 시 직접 catch하지 않고 호출자(JwtAuthenticationFilter)에게 던집니다.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
            throw e; // 예외 다시 던지기
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
            throw e; // 예외 다시 던지기
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
            throw e; // 예외 다시 던지기
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
            throw e; // 예외 다시 던지기
        }
    }

    /**
     * 보조 메서드: 토큰 클레임 추출
     */
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parser().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims(); // 만료된 토큰이라도 클레임은 가져와야 합니다. (Refresh 로직 등에 사용될 수 있음)
        }
    }

    /**
     * ⭐ [추가] 만료된 Access Token에서 Authentication 객체를 가져오는 메서드
     * - Refresh Token 재발급 시 사용됩니다.
     */
    public Authentication getAuthenticationFromExpiredToken(String accessToken) {
        Claims claims = parseClaims(accessToken); // 만료 여부와 상관없이 클레임 추출

        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(role -> "ROLE_" + role.trim())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // UserDetails 객체를 만들어서 Authentication 리턴 (subject는 일반적으로 Member ID)
        UserDetails principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    /**
     * ⭐ [추가] Refresh 로직을 위해 Access Token만 생성하는 보조 메서드
     */
    public String createAccessToken(Member member) { // Member 객체에서 정보를 가져옴
        String authorities = member.getRole().toString();
        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .setSubject(String.valueOf(member.getId()))
                .claim(AUTHORITIES_KEY, authorities)
                .setExpiration(accessTokenExpiresIn)
                .signWith(this.key, SignatureAlgorithm.HS256)
                .compact();
    }
    /**
     * ⭐ [추가] Access Token의 만료 시간(밀리초)를 반환하는 보조 메서드
     */
    public long getAccessTokenExpireTime() {
        return ACCESS_TOKEN_EXPIRE_TIME;
    }
}
