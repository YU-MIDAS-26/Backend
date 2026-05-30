package com.bsight.springserver.domain.user.service;

import com.bsight.springserver.domain.user.dto.request.ChangePasswordRequest;
import com.bsight.springserver.domain.user.dto.request.UpdatePhoneNumberRequest;
import com.bsight.springserver.domain.user.dto.response.UserActionResponse;
import com.bsight.springserver.domain.user.dto.response.UserMeResponse;
import com.bsight.springserver.domain.user.entity.User;
import com.bsight.springserver.domain.user.entity.UserStatus;
import com.bsight.springserver.domain.user.repository.UserRepository;
import com.bsight.springserver.global.exception.CustomException;
import com.bsight.springserver.global.exception.ErrorCode;
import com.bsight.springserver.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserMeResponse getMyInfo() {
        User user = getCurrentUser();

        return UserMeResponse.from(user);
    }

    @Transactional
    public UserActionResponse updatePhoneNumber(UpdatePhoneNumberRequest request) {
        User user = getCurrentUser();

        user.changePhoneNumber(request.phoneNumber().trim());

        return UserActionResponse.phoneUpdated();
    }

    @Transactional
    public UserActionResponse changePassword(ChangePasswordRequest request) {
        if (!request.newPassword().equals(request.newPasswordConfirm())) {
            throw new CustomException(ErrorCode.PASSWORD_CONFIRM_NOT_MATCHED);
        }

        User user = getCurrentUser();
        String encodedPassword = passwordEncoder.encode(request.newPassword());

        user.changePassword(encodedPassword);

        return UserActionResponse.passwordChanged();
    }

    @Transactional
    public UserActionResponse deleteMe() {
        User user = getCurrentUser();

        user.delete();

        return UserActionResponse.deleted();
    }

    private User getCurrentUser() {
        Long userId = getCurrentUserId();

        return userRepository.findByIdAndStatusNot(userId, UserStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        return userDetails.getUserId();
    }
}