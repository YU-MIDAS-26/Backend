package com.bsight.springserver.domain.business.entity;

import com.bsight.springserver.common.entity.BaseEntity;
import com.bsight.springserver.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(
        name = "business_profiles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_business_profiles_user_id", columnNames = "user_id"),
                @UniqueConstraint(name = "uk_business_profiles_registration_number", columnNames = "business_registration_number")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BusinessProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "business_registration_number", nullable = false, length = 30)
    private String businessRegistrationNumber;

    @Column(nullable = false, length = 100)
    private String companyName;

    @Column(nullable = false, length = 30)
    private String representativeName;

    @Column(nullable = false, length = 20)
    private String representativePhone;

    @Column(nullable = false, length = 255)
    private String companyAddress;

    @Column(nullable = false, length = 50)
    private String businessType;

    @Column(nullable = false)
    private LocalDate openingDate;

    @Column(nullable = false, length = 20)
    private String taxType;

    @Column(nullable = false, length = 100)
    private String businessCategory;

    @Column(nullable = false, length = 100)
    private String businessItem;

    @Column(nullable = false, length = 255)
    private String licenseOriginalFileName;

    @Column(nullable = false, length = 255)
    private String licenseStoredFileName;

    @Column(nullable = false, length = 500)
    private String licenseFilePath;

    @Column(nullable = false, length = 100)
    private String licenseContentType;

    @Column(nullable = false)
    private Long licenseFileSize;

    public static BusinessProfile create(
            User user,
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
            String licenseStoredFileName,
            String licenseFilePath,
            String licenseContentType,
            Long licenseFileSize
    ) {
        BusinessProfile businessProfile = new BusinessProfile();
        businessProfile.user = user;
        businessProfile.businessRegistrationNumber = businessRegistrationNumber;
        businessProfile.companyName = companyName;
        businessProfile.representativeName = representativeName;
        businessProfile.representativePhone = representativePhone;
        businessProfile.companyAddress = companyAddress;
        businessProfile.businessType = businessType;
        businessProfile.openingDate = openingDate;
        businessProfile.taxType = taxType;
        businessProfile.businessCategory = businessCategory;
        businessProfile.businessItem = businessItem;
        businessProfile.licenseOriginalFileName = licenseOriginalFileName;
        businessProfile.licenseStoredFileName = licenseStoredFileName;
        businessProfile.licenseFilePath = licenseFilePath;
        businessProfile.licenseContentType = licenseContentType;
        businessProfile.licenseFileSize = licenseFileSize;

        return businessProfile;
    }
}