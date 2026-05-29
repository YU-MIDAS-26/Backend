package com.bsight.springserver.domain.business.repository;

import com.bsight.springserver.domain.business.entity.BusinessProfile;
import com.bsight.springserver.domain.user.entity.User;
import com.bsight.springserver.domain.user.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {

    boolean existsByUser(User user);

    boolean existsByBusinessRegistrationNumber(String businessRegistrationNumber);

    boolean existsByUserId(Long userId);

    Optional<BusinessProfile> findByUserId(Long userId);

    List<BusinessProfile> findAllByUser_Status(UserStatus status);
}