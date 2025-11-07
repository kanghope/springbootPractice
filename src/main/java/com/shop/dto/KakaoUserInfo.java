package com.shop.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class KakaoUserInfo {
    private Long id; // 카카오 고유 ID

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    @Setter
    public static class KakaoAccount {
        private String email;
        private Profile profile;
    }

    @Getter
    @Setter
    public static class Profile {
        private String nickname;
        // @JsonProperty("profile_image_url")
        // private String profileImageUrl;
    }
}
