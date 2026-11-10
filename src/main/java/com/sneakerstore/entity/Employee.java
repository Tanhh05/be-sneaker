package com.sneakerstore.entity;

import com.sneakerstore.entity.BaseEntity;
import lombok.experimental.SuperBuilder;

import com.sneakerstore.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "EMPLOYEES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Employee extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_employees")
    @SequenceGenerator(name = "seq_employees", sequenceName = "SEQ_EMPLOYEES", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "EMPLOYEE_CODE", nullable = false, unique = true, length = 30)
    private String employeeCode;

    @Column(name = "FULL_NAME", nullable = false, length = 100)
    private String fullName;

    @Column(name = "DATE_OF_BIRTH")
    private LocalDate dateOfBirth;

    @Column(name = "GENDER", length = 20)
    private String gender;

    @Column(name = "PHONE", nullable = false, unique = true, length = 20)
    private String phone;

    @Column(name = "EMAIL", unique = true, length = 100)
    private String email;

    @Column(name = "ADDRESS", length = 255)
    private String address;

    @Column(name = "POSITION", nullable = false, length = 100)
    private String position;

    @Column(name = "HIRE_DATE", nullable = false)
    private LocalDate hireDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", unique = true)
    private User user;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;
@PrePersist
    public void prePersist() {
        initAuditTimestamps();
        if (status == null) status = "ACTIVE";
    }
}

