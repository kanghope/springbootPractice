package com.shop.config;

import com.shop.jwt.JwtAccessDeniedHandler; // 403 Forbidden 핸들러 추가
import com.shop.jwt.JwtAuthenticationEntryPoint; // 401 Unauthorized 핸들러 추가
import com.shop.jwt.JwtAuthenticationFilter;
import com.shop.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor; // 생성자 주입을 위한 lombok 추가
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성
public class SecurityConfig {

    // final 필드로 선언하여 생성자 주입으로 변경
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint; // 주입 추가
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;     // 주입 추가

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CORS 설정
                .cors(Customizer.withDefaults())

                // 2. CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                // 3. 세션 관리: 상태 비저장(Stateless)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. 기존 폼 로그인, 로그아웃, OAuth2 로그인 설정 모두 비활성화
                .formLogin(formLogin -> formLogin.disable())
                .logout(logout -> logout.disable())
                //.oauth2Login(oauth2 -> oauth2.disable())

                // 5. JWT 인증 필터 추가
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                )

                // 6. 인가(권한) 설정
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        // 로그인, 회원가입 등 인증이 필요 없는 경로 허용
                        .requestMatchers("/api/auth/**",
                                "/api/members/login",
                                "/api/members/signup",
                                "/api/members/logout",
                                "/api/members/new",
                                // ⭐️ [추가] 카카오 리다이렉트 콜백 API 경로 허용
                                "/api/auth/kakao/callback",
                                "/images/**",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                // ⭐️ [필수 추가] 메인 페이지 상품 목록 API 허용
                                "/api/items",
                                "/api/item/**"
                        ).permitAll()

                        // 관리자 경로는 ADMIN 권한 요구
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ⭐️ [추가] 주문 관련 API는 인증된 사용자만 접근 가능하도록 명시
                        .requestMatchers("/api/order","/api/order/**").authenticated()
                        // ⭐️ [추가] 장바구니 관련 api는 인증된 사용자만 접근 가능하도록 명시
                        .requestMatchers("/api/cart","/api/cart/**").authenticated()


                        // 나머지 모든 API 경로는 인증(토큰) 필요
                        .anyRequest().authenticated()
                )

                // 7. 예외 처리: 인증 및 인가 실패 시 JSON 에러 응답 통합
                .exceptionHandling(handling -> handling
                        // 인증 실패 (401) 시 JSON 응답을 처리할 EntryPoint 설정
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        // 인가 실패 (403) 시 JSON 응답을 처리할 AccessDeniedHandler 설정
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // React 개발 환경의 Origin을 허용
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://127.0.0.1:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
