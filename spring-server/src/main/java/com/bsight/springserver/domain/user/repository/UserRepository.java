package com.bsight.springserver.domain.user.repository;

import com.bsight.springserver.domain.user.entity.User;
import com.bsight.springserver.domain.user.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailAndStatusNot(String email, UserStatus status);

    boolean existsByStudentIdAndStatusNot(String studentId, UserStatus status);
}