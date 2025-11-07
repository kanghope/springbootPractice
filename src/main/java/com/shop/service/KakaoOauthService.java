package com.shop.service;

import com.shop.dto.KakaoTokenDto;
import com.shop.dto.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor

public class KakaoOauthService {
    private final RestTemplate restTemplate;

    // application.yml/properties에 설정된 값들을 주입받습니다.
    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    // (보안) Client Secret은 .yml에 설정하고 주입받는 것이 좋습니다.
     @Value("${kakao.client-secret}")
     private String kakaoClientSecret; // 필요시 활성화

    private static final String KAKAO_TOKEN_URI = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";

    /**
     * 1. 인가 코드로 카카오 액세스 토큰 받기
     */
    public KakaoTokenDto getKakaoToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", code);
        params.add("client_secret", kakaoClientSecret); // Client Secret을 사용하는 경우

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<KakaoTokenDto> response = restTemplate.postForEntity(
                KAKAO_TOKEN_URI,
                request,
                KakaoTokenDto.class
        );

        return response.getBody();
    }

    /**
     * 2. 카카오 액세스 토큰으로 사용자 정보 받기
     */
    public KakaoUserInfo getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

        ResponseEntity<KakaoUserInfo> response = restTemplate.exchange(
                KAKAO_USER_INFO_URI,
                HttpMethod.GET,
                request,
                KakaoUserInfo.class
        );

        return response.getBody();
    }
}
