package com.bsight.springserver.domain.user.entity;

import com.bsight.springserver.common.entity.BaseEntity;
import com.bsight.springserver.global.exception.CustomException;
import com.bsight.springserver.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_student_id", columnNames = "student_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "student_id", nullable = false, length = 50)
    private String studentId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Boolean agreedToTerms1;

    @Column(nullable = false)
    private Boolean agreedToTerms2;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status;

    @Column(length = 500)
    private String rejectionReason;

    public static User createStepOneUser(
            String name,
            LocalDate birthDate,
            String email,
            String phoneNumber,
            String studentId,
            String encodedPassword,
            Boolean agreedToTerms1,
            Boolean agreedToTerms2
    ) {
        User user = new User();
        user.name = name;
        user.birthDate = birthDate;
        user.email = email;
        user.phoneNumber = phoneNumber;
        user.studentId = studentId;
        user.password = encodedPassword;
        user.agreedToTerms1 = agreedToTerms1;
        user.agreedToTerms2 = agreedToTerms2;
        user.role = UserRole.USER;
        user.status = UserStatus.PENDING_BUSINESS;

        return user;
    }

    public void changeToPendingApproval() {
        if (this.status != UserStatus.PENDING_BUSINESS) {
            throw new CustomException(ErrorCode.INVALID_USER_STATUS);
        }

        this.status = UserStatus.PENDING_APPROVAL;
        this.rejectionReason = null;
    }

    public void approve() {
        if (this.status != UserStatus.PENDING_APPROVAL) {
            throw new CustomException(ErrorCode.INVALID_USER_STATUS);
        }

        this.status = UserStatus.ACTIVE;
        this.rejectionReason = null;
    }

    public void reject(String rejectionReason) {
        if (this.status != UserStatus.PENDING_APPROVAL) {
            throw new CustomException(ErrorCode.INVALID_USER_STATUS);
        }

        this.status = UserStatus.REJECTED;
        this.rejectionReason = rejectionReason;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void delete() {
        this.status = UserStatus.DELETED;
    }
}