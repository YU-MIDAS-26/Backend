package com.bsight.springserver.domain.employee.service;

import com.bsight.springserver.domain.employee.dto.request.EmployeeCreateRequest;
import com.bsight.springserver.domain.employee.dto.request.EmployeeUpdateRequest;
import com.bsight.springserver.domain.employee.dto.response.EmployeeActionResponse;
import com.bsight.springserver.domain.employee.dto.response.EmployeeResponse;
import com.bsight.springserver.domain.employee.entity.Employee;
import com.bsight.springserver.domain.employee.entity.EmployeeStatus;
import com.bsight.springserver.domain.employee.repository.EmployeeRepository;
import com.bsight.springserver.domain.user.entity.User;
import com.bsight.springserver.domain.user.entity.UserStatus;
import com.bsight.springserver.domain.user.repository.UserRepository;
import com.bsight.springserver.global.exception.CustomException;
import com.bsight.springserver.global.exception.ErrorCode;
import com.bsight.springserver.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @Transactional
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        User user = getCurrentUser();
        String employeeNumber = normalizeEmployeeNumber(request.employeeNumber());

        validateEmployeeNumberDuplicate(user, employeeNumber);

        Employee employee = Employee.create(
                user,
                request.name().trim(),
                request.birthDate(),
                request.phoneNumber().trim(),
                employeeNumber,
                request.payType(),
                request.payAmount(),
                request.weeklyHolidayPayApplied()
        );

        Employee savedEmployee = employeeRepository.save(employee);

        return EmployeeResponse.from(savedEmployee);
    }

    public List<EmployeeResponse> getEmployees() {
        User user = getCurrentUser();

        return employeeRepository.findAllByUserAndStatusOrderByCreatedAtDesc(user, EmployeeStatus.ACTIVE)
                .stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    public EmployeeResponse getEmployee(Long employeeId) {
        User user = getCurrentUser();
        Employee employee = getActiveEmployee(employeeId, user);

        return EmployeeResponse.from(employee);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long employeeId, EmployeeUpdateRequest request) {
        User user = getCurrentUser();
        Employee employee = getActiveEmployee(employeeId, user);
        String employeeNumber = normalizeEmployeeNumber(request.employeeNumber());

        validateEmployeeNumberDuplicateForUpdate(user, employeeNumber, employeeId);

        employee.update(
                request.name().trim(),
                request.birthDate(),
                request.phoneNumber().trim(),
                employeeNumber,
                request.payType(),
                request.payAmount(),
                request.weeklyHolidayPayApplied()
        );

        return EmployeeResponse.from(employee);
    }

    @Transactional
    public EmployeeActionResponse deleteEmployee(Long employeeId) {
        User user = getCurrentUser();
        Employee employee = getActiveEmployee(employeeId, user);

        employee.delete();

        return EmployeeActionResponse.deleted();
    }

    private Employee getActiveEmployee(Long employeeId, User user) {
        return employeeRepository.findByIdAndUserAndStatus(employeeId, user, EmployeeStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    private void validateEmployeeNumberDuplicate(User user, String employeeNumber) {
        if (employeeNumber == null) {
            return;
        }

        if (employeeRepository.existsByUserAndEmployeeNumberAndStatus(user, employeeNumber, EmployeeStatus.ACTIVE)) {
            throw new CustomException(ErrorCode.EMPLOYEE_NUMBER_ALREADY_EXISTS);
        }
    }

    private void validateEmployeeNumberDuplicateForUpdate(User user, String employeeNumber, Long employeeId) {
        if (employeeNumber == null) {
            return;
        }

        if (employeeRepository.existsByUserAndEmployeeNumberAndStatusAndIdNot(
                user,
                employeeNumber,
                EmployeeStatus.ACTIVE,
                employeeId
        )) {
            throw new CustomException(ErrorCode.EMPLOYEE_NUMBER_ALREADY_EXISTS);
        }
    }

    private String normalizeEmployeeNumber(String employeeNumber) {
        if (employeeNumber == null || employeeNumber.trim().isBlank()) {
            return null;
        }

        return employeeNumber.trim();
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