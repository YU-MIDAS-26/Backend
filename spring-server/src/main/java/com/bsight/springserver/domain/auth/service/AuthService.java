package com.bsight.springserver.domain.auth.service;

import com.bsight.springserver.domain.auth.dto.request.EmailVerificationConfirmRequest;
import com.bsight.springserver.domain.auth.dto.request.EmailVerificationRequest;
import com.bsight.springserver.domain.auth.dto.request.LoginRequest;
import com.bsight.springserver.domain.auth.dto.request.PasswordResetConfirmRequest;
import com.bsight.springserver.domain.auth.dto.request.PasswordResetRequest;
import com.bsight.springserver.domain.auth.dto.request.RegisterStepOneRequest;
import com.bsight.springserver.domain.auth.dto.request.RegisterStepTwoRequest;
import com.bsight.springserver.domain.auth.dto.request.StudentIdCheckRequest;
import com.bsight.springserver.domain.auth.dto.response.LoginResponse;
import com.bsight.springserver.domain.auth.dto.response.LogoutResponse;
import com.bsight.springserver.domain.auth.dto.response.PasswordResetResponse;
import com.bsight.springserver.domain.auth.dto.response.RegisterStepTwoResponse;
import com.bsight.springserver.domain.auth.entity.EmailVerification;
import com.bsight.springserver.domain.auth.entity.JwtBlacklistToken;
import com.bsight.springserver.domain.auth.entity.PasswordResetToken;
import com.bsight.springserver.domain.auth.repository.EmailVerificationRepository;
import com.bsight.springserver.domain.auth.repository.JwtBlacklistTokenRepository;
import com.bsight.springserver.domain.auth.repository.PasswordResetTokenRepository;
import com.bsight.springserver.domain.business.entity.BusinessProfile;
import com.bsight.springserver.domain.business.repository.BusinessProfileRepository;
import com.bsight.springserver.domain.business.service.BusinessLicenseFileStorageService;
import com.bsight.springserver.domain.business.service.BusinessLicenseFileStorageService.StoredBusinessLicenseFile;
import com.bsight.springserver.domain.user.entity.User;
import com.bsight.springserver.domain.user.entity.UserStatus;
import com.bsight.springserver.domain.user.repository.UserRepository;
import com.bsight.springserver.global.exception.CustomException;
import com.bsight.springserver.global.exception.ErrorCode;
import com.bsight.springserver.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final int EMAIL_VERIFICATION_EXPIRE_MINUTES = 5;
    private static final int PASSWORD_RESET_EXPIRE_MINUTES = 10;
    private static final String BEARER_PREFIX = "Bearer ";

    private final SecureRandom secureRandom = new SecureRandom();

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessLicenseFileStorageService businessLicenseFileStorageService;
    private final JwtBlacklistTokenRepository jwtBlacklistTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final JwtTokenProvider jwtTokenProvider;

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

    @Transactional
    public RegisterStepTwoResponse registerStepTwo(RegisterStepTwoRequest request) {
        String studentId = request.studentId().trim();
        String businessRegistrationNumber = request.businessRegistrationNumber().trim();

        User user = userRepository.findByStudentIdAndStatusNot(studentId, UserStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_STEP_ONE_NOT_FOUND));

        if (user.getStatus() != UserStatus.PENDING_BUSINESS) {
            throw new CustomException(ErrorCode.INVALID_USER_STATUS);
        }

        if (businessProfileRepository.existsByUser(user)) {
            throw new CustomException(ErrorCode.BUSINESS_PROFILE_ALREADY_EXISTS);
        }

        if (businessProfileRepository.existsByBusinessRegistrationNumber(businessRegistrationNumber)) {
            throw new CustomException(ErrorCode.BUSINESS_REGISTRATION_NUMBER_ALREADY_EXISTS);
        }

        StoredBusinessLicenseFile storedFile = businessLicenseFileStorageService.store(request.businessLicenseFile());

        BusinessProfile businessProfile = BusinessProfile.create(
                user,
                businessRegistrationNumber,
                request.companyName().trim(),
                request.representativeName().trim(),
                request.representativePhone().trim(),
                request.companyAddress().trim(),
                request.businessType().trim(),
                request.openingDate(),
                request.taxType().trim(),
                request.businessCategory().trim(),
                request.businessItem().trim(),
                storedFile.originalFileName(),
                storedFile.storedFileName(),
                storedFile.filePath(),
                storedFile.contentType(),
                storedFile.fileSize()
        );

        businessProfileRepository.save(businessProfile);
        user.changeToPendingApproval();

        return RegisterStepTwoResponse.of(
                user.getId(),
                businessProfile.getId(),
                user.getStatus()
        );
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByStudentIdAndStatusNot(request.studentId().trim(), UserStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_LOGIN));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_LOGIN);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new CustomException(ErrorCode.LOGIN_NOT_ALLOWED);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user);

        return LoginResponse.of(
                accessToken,
                jwtTokenProvider.getExpiration(),
                user
        );
    }

    @Transactional
    public LogoutResponse logout(String authorizationHeader) {
        String token = extractAccessToken(authorizationHeader);

        jwtTokenProvider.validateToken(token);

        if (jwtBlacklistTokenRepository.existsByToken(token)) {
            throw new CustomException(ErrorCode.ALREADY_LOGGED_OUT);
        }

        JwtBlacklistToken blacklistToken = JwtBlacklistToken.create(
                token,
                jwtTokenProvider.getTokenExpiresAt(token)
        );

        jwtBlacklistTokenRepository.save(blacklistToken);

        return LogoutResponse.loggedOut();
    }

    @Transactional
    public PasswordResetResponse requestPasswordReset(PasswordResetRequest request) {
        String studentId = request.studentId().trim();
        String email = request.email().trim();

        User user = userRepository.findByStudentIdAndEmailAndStatusNot(studentId, email, UserStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.PASSWORD_RESET_USER_NOT_FOUND));

        String token = UUID.randomUUID().toString();

        PasswordResetToken passwordResetToken = PasswordResetToken.create(
                token,
                user,
                LocalDateTime.now().plusMinutes(PASSWORD_RESET_EXPIRE_MINUTES)
        );

        passwordResetTokenRepository.save(passwordResetToken);
        mailService.sendPasswordResetLink(email, token);

        return PasswordResetResponse.requested();
    }

    @Transactional
    public PasswordResetResponse confirmPasswordReset(PasswordResetConfirmRequest request) {
        if (!request.newPassword().equals(request.newPasswordConfirm())) {
            throw new CustomException(ErrorCode.PASSWORD_CONFIRM_NOT_MATCHED);
        }

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(request.token().trim())
                .orElseThrow(() -> new CustomException(ErrorCode.PASSWORD_RESET_TOKEN_NOT_FOUND));

        if (passwordResetToken.isUsed()) {
            throw new CustomException(ErrorCode.PASSWORD_RESET_TOKEN_ALREADY_USED);
        }

        if (passwordResetToken.isExpired(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.PASSWORD_RESET_TOKEN_EXPIRED);
        }

        User user = passwordResetToken.getUser();
        String encodedPassword = passwordEncoder.encode(request.newPassword());

        user.changePassword(encodedPassword);
        passwordResetToken.use();

        return PasswordResetResponse.completed();
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

    private String extractAccessToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new CustomException(ErrorCode.TOKEN_REQUIRED);
        }

        return authorizationHeader.substring(BEARER_PREFIX.length());
    }

    private String generateVerificationCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }
}