package com.bsight.springserver.domain.employee.repository;

import com.bsight.springserver.domain.employee.entity.Employee;
import com.bsight.springserver.domain.employee.entity.EmployeeStatus;
import com.bsight.springserver.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findAllByUserAndStatusOrderByCreatedAtDesc(User user, EmployeeStatus status);

    Optional<Employee> findByIdAndUserAndStatus(Long id, User user, EmployeeStatus status);

    boolean existsByUserAndEmployeeNumberAndStatus(User user, String employeeNumber, EmployeeStatus status);

    boolean existsByUserAndEmployeeNumberAndStatusAndIdNot(
            User user,
            String employeeNumber,
            EmployeeStatus status,
            Long id
    );
}