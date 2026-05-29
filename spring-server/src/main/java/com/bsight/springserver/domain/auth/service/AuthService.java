package com.bsight.springserver.domain.auth.service;

import com.bsight.springserver.domain.auth.dto.request.EmailVerificationConfirmRequest;
import com.bsight.springserver.domain.auth.dto.request.EmailVerificationRequest;
import com.bsight.springserver.domain.auth.dto.request.RegisterStepOneRequest;
import com.bsight.springserver.domain.auth.dto.request.StudentIdCheckRequest;
import com.bsight.springserver.domain.auth.entity.EmailVerification;
import com.bsight.springserver.domain.auth.repository.EmailVerificationRepository;
import com.bsight.springserver.domain.user.entity.User;
import com.bsight.springserver.domain.user.entity.UserStatus;
import com.bsight.springserver.domain.user.repository.UserRepository;
import com.bsight.springserver.global.exception.CustomException;
import com.bsight.springserver.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final int EMAIL_VERIFICATION_EXPIRE_MINUTES = 5;

    private final SecureRandom secureRandom = new SecureRandom();

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Transactional
    public void requestEmailVerificationCode(EmailVerificationRequest request) {
        String email = request.email().trim();

        if (userRepository.existsByEmailAndStatusNot(email, UserStatus.DELETED)) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String code = generateVerificationCode();

        EmailVerification emailVerification = EmailVerification.create(
                email,
                code,
                LocalDateTime.now().plusMinutes(EMAIL_VERIFICATION_EXPIRE_MINUTES)
        );

        emailVerificationRepository.save(emailVerification);
        mailService.sendEmailVerificationCode(email, code);
    }

    @Transactional
    public void verifyEmailVerificationCode(EmailVerificationConfirmRequest request) {
        String email = request.email().trim();
        String code = request.code().trim();

        EmailVerification emailVerification = emailVerificationRepository
                .findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_VERIFICATION_NOT_FOUND));

        if (emailVerification.isExpired(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_EXPIRED);
        }

        if (!emailVerification.isCodeMatched(code)) {
            throw new CustomException(ErrorCode.INVALID_EMAIL_VERIFICATION_CODE);
        }

        emailVerification.verify();
    }

    public void checkStudentIdDuplicate(StudentIdCheckRequest request) {
        String studentId = request.studentId().trim();

        if (userRepository.existsByStudentIdAndStatusNot(studentId, UserStatus.DELETED)) {
            throw new CustomException(ErrorCode.STUDENT_ID_ALREADY_EXISTS);
        }
    }

    @Transactional
    public void registerStepOne(RegisterStepOneRequest request) {
        String email = request.email().trim();
        String studentId = request.studentId().trim();

        validateRegisterStepOne(request, email, studentId);

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.createStepOneUser(
                request.name().trim(),
                request.birthDate(),
                email,
                request.phoneNumber().trim(),
                studentId,
                encodedPassword,
                request.agreedToTerms1(),
                request.agreedToTerms2()
        );

        userRepository.save(user);
    }

    private void validateRegisterStepOne(RegisterStepOneRequest request, String email, String studentId) {
        if (!request.password().equals(request.passwordConfirm())) {
            throw new CustomException(ErrorCode.PASSWORD_CONFIRM_NOT_MATCHED);
        }

        if (!Boolean.TRUE.equals(request.agreedToTerms1()) || !Boolean.TRUE.equals(request.agreedToTerms2())) {
            throw new CustomException(ErrorCode.TERMS_NOT_AGREED);
        }

        if (userRepository.existsByEmailAndStatusNot(email, UserStatus.DELETED)) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (userRepository.existsByStudentIdAndStatusNot(studentId, UserStatus.DELETED)) {
            throw new CustomException(ErrorCode.STUDENT_ID_ALREADY_EXISTS);
        }

        EmailVerification emailVerification = emailVerificationRepository
                .findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_VERIFICATION_NOT_FOUND));

        if (emailVerification.isExpired(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_EXPIRED);
        }

        if (!emailVerification.isCodeMatched(request.emailVerificationCode().trim())) {
            throw new CustomException(ErrorCode.INVALID_EMAIL_VERIFICATION_CODE);
        }

        if (!Boolean.TRUE.equals(emailVerification.getVerified())) {
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
    }

    private String generateVerificationCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }
}