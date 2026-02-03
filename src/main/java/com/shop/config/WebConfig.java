package com.shop.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer{
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // /auth/social/callback 뿐만 아니라 리액트의 모든 라우팅 경로를 index.html로 매핑
        registry.addViewController("/auth/social/callback").setViewName("forward:/index.html");
        registry.addViewController("/members/login").setViewName("forward:/index.html");
        registry.addViewController("/orders").setViewName("forward:/index.html");
        // 추가로 필요한 리액트 경로들을 여기에 계속 등록하세요.
    }
}
