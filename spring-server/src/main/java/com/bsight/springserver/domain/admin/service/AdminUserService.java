package com.bsight.springserver.domain.admin.service;

import com.bsight.springserver.domain.admin.dto.request.AdminUserRejectRequest;
import com.bsight.springserver.domain.admin.dto.response.AdminPendingUserResponse;
import com.bsight.springserver.domain.admin.dto.response.AdminUserApprovalResponse;
import com.bsight.springserver.domain.admin.dto.response.AdminUserListResponse;
import com.bsight.springserver.domain.business.repository.BusinessProfileRepository;
import com.bsight.springserver.domain.user.entity.User;
import com.bsight.springserver.domain.user.entity.UserStatus;
import com.bsight.springserver.domain.user.repository.UserRepository;
import com.bsight.springserver.global.exception.CustomException;
import com.bsight.springserver.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;
    private final BusinessProfileRepository businessProfileRepository;

    public List<AdminUserListResponse> getUsers() {
        return userRepository.findAllByStatusNotOrderByCreatedAtDesc(UserStatus.DELETED)
                .stream()
                .map(AdminUserListResponse::from)
                .toList();
    }

    public List<AdminPendingUserResponse> getPendingApprovalUsers() {
        return businessProfileRepository.findAllByUser_Status(UserStatus.PENDING_APPROVAL)
                .stream()
                .map(AdminPendingUserResponse::from)
                .toList();
    }

    @Transactional
    public AdminUserApprovalResponse approveUser(Long userId) {
        User user = getUser(userId);
        validateBusinessProfileExists(userId);

        user.approve();

        return AdminUserApprovalResponse.approved(user);
    }

    @Transactional
    public AdminUserApprovalResponse rejectUser(Long userId, AdminUserRejectRequest request) {
        User user = getUser(userId);
        validateBusinessProfileExists(userId);

        user.reject(request.rejectionReason().trim());

        return AdminUserApprovalResponse.rejected(user);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateBusinessProfileExists(Long userId) {
        if (!businessProfileRepository.existsByUserId(userId)) {
            throw new CustomException(ErrorCode.BUSINESS_PROFILE_NOT_FOUND);
        }
    }
}