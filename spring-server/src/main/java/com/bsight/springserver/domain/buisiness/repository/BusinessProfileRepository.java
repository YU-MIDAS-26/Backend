package com.bsight.springserver.domain.business.repository;

import com.bsight.springserver.domain.business.entity.BusinessProfile;
import com.bsight.springserver.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {

    boolean existsByUser(User user);

    boolean existsByBusinessRegistrationNumber(String businessRegistrationNumber);
}