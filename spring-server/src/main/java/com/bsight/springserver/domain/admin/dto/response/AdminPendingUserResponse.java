package com.bsight.springserver.domain.admin.dto.response;

import com.bsight.springserver.domain.business.entity.BusinessProfile;
import com.bsight.springserver.domain.user.entity.User;
import com.bsight.springserver.domain.user.entity.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminPendingUserResponse(
        Long userId,
        String studentId,
        String name,
        String email,
        String phoneNumber,
        UserStatus status,

        Long businessProfileId,
        String businessRegistrationNumber,
        String companyName,
        String representativeName,
        String representativePhone,
        String companyAddress,
        String businessType,
        LocalDate openingDate,
        String taxType,
        String businessCategory,
        String businessItem,

        String licenseOriginalFileName,
        Long licenseFileSize,
        LocalDateTime submittedAt
) {

    public static AdminPendingUserResponse from(BusinessProfile businessProfile) {
        User user = businessProfile.getUser();

        return new AdminPendingUserResponse(
                user.getId(),
                user.getStudentId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getStatus(),

                businessProfile.getId(),
                businessProfile.getBusinessRegistrationNumber(),
                businessProfile.getCompanyName(),
                businessProfile.getRepresentativeName(),
                businessProfile.getRepresentativePhone(),
                businessProfile.getCompanyAddress(),
                businessProfile.getBusinessType(),
                businessProfile.getOpeningDate(),
                businessProfile.getTaxType(),
                businessProfile.getBusinessCategory(),
                businessProfile.getBusinessItem(),

                businessProfile.getLicenseOriginalFileName(),
                businessProfile.getLicenseFileSize(),
                businessProfile.getCreatedAt()
        );
    }
}