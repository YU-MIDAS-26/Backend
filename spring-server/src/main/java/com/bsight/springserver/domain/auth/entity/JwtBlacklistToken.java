package com.bsight.springserver.domain.auth.entity;

import com.bsight.springserver.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "jwt_blacklist_tokens",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_jwt_blacklist_tokens_token", columnNames = "token")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JwtBlacklistToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public static JwtBlacklistToken create(String token, LocalDateTime expiresAt) {
        JwtBlacklistToken jwtBlacklistToken = new JwtBlacklistToken();
        jwtBlacklistToken.token = token;
        jwtBlacklistToken.expiresAt = expiresAt;

        return jwtBlacklistToken;
    }

    public boolean isExpired(LocalDateTime now) {
        return expiresAt.isBefore(now);
    }
}