package com.bsight.springserver.domain.auth.entity;

import com.bsight.springserver.common.entity.BaseEntity;
import com.bsight.springserver.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "password_reset_tokens",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_password_reset_tokens_token", columnNames = "token")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Boolean used;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public static PasswordResetToken create(String token, User user, LocalDateTime expiresAt) {
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.token = token;
        passwordResetToken.user = user;
        passwordResetToken.used = false;
        passwordResetToken.expiresAt = expiresAt;

        return passwordResetToken;
    }

    public void use() {
        this.used = true;
    }

    public boolean isExpired(LocalDateTime now) {
        return expiresAt.isBefore(now);
    }

    public boolean isUsed() {
        return Boolean.TRUE.equals(used);
    }
}