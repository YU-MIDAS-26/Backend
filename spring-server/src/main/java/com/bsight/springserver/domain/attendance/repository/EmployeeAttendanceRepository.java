package com.bsight.springserver.domain.attendance.repository;

import com.bsight.springserver.domain.attendance.entity.AttendanceStatus;
import com.bsight.springserver.domain.attendance.entity.EmployeeAttendance;
import com.bsight.springserver.domain.employee.entity.Employee;
import com.bsight.springserver.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeAttendanceRepository extends JpaRepository<EmployeeAttendance, Long> {

    List<EmployeeAttendance> findAllByUserAndWorkDateAndStatusOrderByCheckInTimeAsc(
            User user,
            LocalDate workDate,
            AttendanceStatus status
    );

    Optional<EmployeeAttendance> findByIdAndUserAndStatus(
            Long id,
            User user,
            AttendanceStatus status
    );

    boolean existsByUserAndEmployeeAndWorkDateAndStatus(
            User user,
            Employee employee,
            LocalDate workDate,
            AttendanceStatus status
    );

    boolean existsByUserAndEmployeeAndWorkDateAndStatusAndIdNot(
            User user,
            Employee employee,
            LocalDate workDate,
            AttendanceStatus status,
            Long id
    );
}