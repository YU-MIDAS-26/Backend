package com.bsight.springserver.domain.employee.entity;

import com.bsight.springserver.common.entity.BaseEntity;
import com.bsight.springserver.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "employees")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Employee extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(length = 50)
    private String employeeNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayType payType;

    @Column(nullable = false)
    private Long payAmount;

    @Column(nullable = false)
    private Boolean weeklyHolidayPayApplied;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmployeeStatus status;

    public static Employee create(
            User user,
            String name,
            LocalDate birthDate,
            String phoneNumber,
            String employeeNumber,
            PayType payType,
            Long payAmount,
            Boolean weeklyHolidayPayApplied
    ) {
        Employee employee = new Employee();
        employee.user = user;
        employee.name = name;
        employee.birthDate = birthDate;
        employee.phoneNumber = phoneNumber;
        employee.employeeNumber = employeeNumber;
        employee.payType = payType;
        employee.payAmount = payAmount;
        employee.weeklyHolidayPayApplied = weeklyHolidayPayApplied;
        employee.status = EmployeeStatus.ACTIVE;

        return employee;
    }

    public void update(
            String name,
            LocalDate birthDate,
            String phoneNumber,
            String employeeNumber,
            PayType payType,
            Long payAmount,
            Boolean weeklyHolidayPayApplied
    ) {
        this.name = name;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.employeeNumber = employeeNumber;
        this.payType = payType;
        this.payAmount = payAmount;
        this.weeklyHolidayPayApplied = weeklyHolidayPayApplied;
    }

    public void delete() {
        this.status = EmployeeStatus.DELETED;
    }
}