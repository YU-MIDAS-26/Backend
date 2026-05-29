package com.bsight.springserver.domain.auth.repository;

import com.bsight.springserver.domain.auth.entity.JwtBlacklistToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JwtBlacklistTokenRepository extends JpaRepository<JwtBlacklistToken, Long> {

    boolean existsByToken(String token);
}