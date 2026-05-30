package com.bsight.springserver.domain.auth.entity;

import com.bsight.springserver.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "email_verifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false)
    private Boolean verified;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public static EmailVerification create(String email, String code, LocalDateTime expiresAt) {
        EmailVerification emailVerification = new EmailVerification();
        emailVerification.email = email;
        emailVerification.code = code;
        emailVerification.verified = false;
        emailVerification.expiresAt = expiresAt;

        return emailVerification;
    }

    public void verify() {
        this.verified = true;
    }

    public boolean isExpired(LocalDateTime now) {
        return expiresAt.isBefore(now);
    }

    public boolean isCodeMatched(String inputCode) {
        return code.equals(inputCode);
    }
}