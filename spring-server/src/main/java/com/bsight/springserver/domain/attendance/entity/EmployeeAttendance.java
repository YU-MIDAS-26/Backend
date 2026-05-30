package com.bsight.springserver.domain.attendance.entity;

import com.bsight.springserver.common.entity.BaseEntity;
import com.bsight.springserver.domain.employee.entity.Employee;
import com.bsight.springserver.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Entity
@Table(
        name = "employee_attendances",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_employee_attendances_user_employee_date_status",
                        columnNames = {"user_id", "employee_id", "work_date", "status"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmployeeAttendance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(nullable = false)
    private LocalTime checkInTime;

    @Column(nullable = false)
    private LocalTime checkOutTime;

    @Column(nullable = false)
    private Boolean breakTimeApplied;

    private LocalTime breakStartTime;

    private LocalTime breakEndTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    public static EmployeeAttendance create(
            User user,
            Employee employee,
            LocalDate workDate,
            LocalTime checkInTime,
            LocalTime checkOutTime,
            Boolean breakTimeApplied,
            LocalTime breakStartTime,
            LocalTime breakEndTime
    ) {
        EmployeeAttendance attendance = new EmployeeAttendance();
        attendance.user = user;
        attendance.employee = employee;
        attendance.workDate = workDate;
        attendance.checkInTime = checkInTime;
        attendance.checkOutTime = checkOutTime;
        attendance.breakTimeApplied = breakTimeApplied;
        attendance.breakStartTime = breakStartTime;
        attendance.breakEndTime = breakEndTime;
        attendance.status = AttendanceStatus.ACTIVE;

        return attendance;
    }

    public void update(
            Employee employee,
            LocalDate workDate,
            LocalTime checkInTime,
            LocalTime checkOutTime,
            Boolean breakTimeApplied,
            LocalTime breakStartTime,
            LocalTime breakEndTime
    ) {
        this.employee = employee;
        this.workDate = workDate;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.breakTimeApplied = breakTimeApplied;
        this.breakStartTime = breakStartTime;
        this.breakEndTime = breakEndTime;
    }

    public void delete() {
        this.status = AttendanceStatus.DELETED;
    }
}