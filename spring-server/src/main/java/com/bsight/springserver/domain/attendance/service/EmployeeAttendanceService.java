package com.bsight.springserver.domain.attendance.service;

import com.bsight.springserver.domain.attendance.dto.request.EmployeeAttendanceCreateRequest;
import com.bsight.springserver.domain.attendance.dto.request.EmployeeAttendanceUpdateRequest;
import com.bsight.springserver.domain.attendance.dto.response.EmployeeAttendanceActionResponse;
import com.bsight.springserver.domain.attendance.dto.response.EmployeeAttendanceResponse;
import com.bsight.springserver.domain.attendance.entity.AttendanceStatus;
import com.bsight.springserver.domain.attendance.entity.EmployeeAttendance;
import com.bsight.springserver.domain.attendance.repository.EmployeeAttendanceRepository;
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

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeAttendanceService {

    private final EmployeeAttendanceRepository employeeAttendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @Transactional
    public EmployeeAttendanceResponse createAttendance(EmployeeAttendanceCreateRequest request) {
        User user = getCurrentUser();
        Employee employee = getActiveEmployee(request.employeeId(), user);

        validateWorkTime(request.checkInTime(), request.checkOutTime());
        validateBreakTime(
                request.checkInTime(),
                request.checkOutTime(),
                request.breakTimeApplied(),
                request.breakStartTime(),
                request.breakEndTime()
        );
        validateAttendanceDuplicate(user, employee, request);

        EmployeeAttendance attendance = EmployeeAttendance.create(
                user,
                employee,
                request.workDate(),
                request.checkInTime(),
                request.checkOutTime(),
                request.breakTimeApplied(),
                normalizeBreakStartTime(request),
                normalizeBreakEndTime(request)
        );

        EmployeeAttendance savedAttendance = employeeAttendanceRepository.save(attendance);

        return EmployeeAttendanceResponse.from(savedAttendance);
    }

    public List<EmployeeAttendanceResponse> getAttendancesByDate(java.time.LocalDate workDate) {
        User user = getCurrentUser();

        return employeeAttendanceRepository
                .findAllByUserAndWorkDateAndStatusOrderByCheckInTimeAsc(
                        user,
                        workDate,
                        AttendanceStatus.ACTIVE
                )
                .stream()
                .map(EmployeeAttendanceResponse::from)
                .toList();
    }

    public EmployeeAttendanceResponse getAttendance(Long attendanceId) {
        User user = getCurrentUser();
        EmployeeAttendance attendance = getActiveAttendance(attendanceId, user);

        return EmployeeAttendanceResponse.from(attendance);
    }

    @Transactional
    public EmployeeAttendanceResponse updateAttendance(
            Long attendanceId,
            EmployeeAttendanceUpdateRequest request
    ) {
        User user = getCurrentUser();
        EmployeeAttendance attendance = getActiveAttendance(attendanceId, user);
        Employee employee = getActiveEmployee(request.employeeId(), user);

        validateWorkTime(request.checkInTime(), request.checkOutTime());
        validateBreakTime(
                request.checkInTime(),
                request.checkOutTime(),
                request.breakTimeApplied(),
                request.breakStartTime(),
                request.breakEndTime()
        );
        validateAttendanceDuplicateForUpdate(user, employee, request, attendanceId);

        attendance.update(
                employee,
                request.workDate(),
                request.checkInTime(),
                request.checkOutTime(),
                request.breakTimeApplied(),
                normalizeBreakStartTime(request),
                normalizeBreakEndTime(request)
        );

        return EmployeeAttendanceResponse.from(attendance);
    }

    @Transactional
    public EmployeeAttendanceActionResponse deleteAttendance(Long attendanceId) {
        User user = getCurrentUser();
        EmployeeAttendance attendance = getActiveAttendance(attendanceId, user);

        attendance.delete();

        return EmployeeAttendanceActionResponse.deleted();
    }

    private void validateWorkTime(LocalTime checkInTime, LocalTime checkOutTime) {
        if (!checkOutTime.isAfter(checkInTime)) {
            throw new CustomException(ErrorCode.INVALID_WORK_TIME);
        }
    }

    private void validateBreakTime(
            LocalTime checkInTime,
            LocalTime checkOutTime,
            Boolean breakTimeApplied,
            LocalTime breakStartTime,
            LocalTime breakEndTime
    ) {
        if (!Boolean.TRUE.equals(breakTimeApplied)) {
            return;
        }

        if (breakStartTime == null || breakEndTime == null) {
            throw new CustomException(ErrorCode.BREAK_TIME_REQUIRED);
        }

        boolean validBreakTime =
                breakStartTime.isAfter(checkInTime)
                        && breakEndTime.isBefore(checkOutTime)
                        && breakEndTime.isAfter(breakStartTime);

        if (!validBreakTime) {
            throw new CustomException(ErrorCode.INVALID_BREAK_TIME);
        }
    }

    private void validateAttendanceDuplicate(
            User user,
            Employee employee,
            EmployeeAttendanceCreateRequest request
    ) {
        boolean exists = employeeAttendanceRepository.existsByUserAndEmployeeAndWorkDateAndStatus(
                user,
                employee,
                request.workDate(),
                AttendanceStatus.ACTIVE
        );

        if (exists) {
            throw new CustomException(ErrorCode.ATTENDANCE_ALREADY_EXISTS);
        }
    }

    private void validateAttendanceDuplicateForUpdate(
            User user,
            Employee employee,
            EmployeeAttendanceUpdateRequest request,
            Long attendanceId
    ) {
        boolean exists = employeeAttendanceRepository.existsByUserAndEmployeeAndWorkDateAndStatusAndIdNot(
                user,
                employee,
                request.workDate(),
                AttendanceStatus.ACTIVE,
                attendanceId
        );

        if (exists) {
            throw new CustomException(ErrorCode.ATTENDANCE_ALREADY_EXISTS);
        }
    }

    private EmployeeAttendance getActiveAttendance(Long attendanceId, User user) {
        return employeeAttendanceRepository.findByIdAndUserAndStatus(
                        attendanceId,
                        user,
                        AttendanceStatus.ACTIVE
                )
                .orElseThrow(() -> new CustomException(ErrorCode.ATTENDANCE_NOT_FOUND));
    }

    private Employee getActiveEmployee(Long employeeId, User user) {
        return employeeRepository.findByIdAndUserAndStatus(employeeId, user, EmployeeStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    private LocalTime normalizeBreakStartTime(EmployeeAttendanceCreateRequest request) {
        if (!Boolean.TRUE.equals(request.breakTimeApplied())) {
            return null;
        }

        return request.breakStartTime();
    }

    private LocalTime normalizeBreakEndTime(EmployeeAttendanceCreateRequest request) {
        if (!Boolean.TRUE.equals(request.breakTimeApplied())) {
            return null;
        }

        return request.breakEndTime();
    }

    private LocalTime normalizeBreakStartTime(EmployeeAttendanceUpdateRequest request) {
        if (!Boolean.TRUE.equals(request.breakTimeApplied())) {
            return null;
        }

        return request.breakStartTime();
    }

    private LocalTime normalizeBreakEndTime(EmployeeAttendanceUpdateRequest request) {
        if (!Boolean.TRUE.equals(request.breakTimeApplied())) {
            return null;
        }

        return request.breakEndTime();
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