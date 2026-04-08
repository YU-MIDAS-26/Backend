package com.example.yumidasbackend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

@Configuration
public class UserDetailsServiceConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // JWT 필터가 토큰의 subject(email)로 사용자 조회를 시도하므로
            // 현재 사용자 도메인이 없을 때 임시로 기본 principal을 반환한다.
            UserDetails user = new User(
                    username,
                    "",
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            return user;
        };
    }
}
