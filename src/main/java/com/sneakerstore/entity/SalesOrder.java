package com.sneakerstore.entity;

import com.sneakerstore.entity.BaseEntity;
import lombok.experimental.SuperBuilder;

import com.sneakerstore.entity.Customer;
import com.sneakerstore.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SALES_ORDERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SalesOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_sales_orders")
    @SequenceGenerator(name = "seq_sales_orders", sequenceName = "SEQ_SALES_ORDERS", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ORDER_CODE", nullable = false, unique = true, length = 30)
    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMPLOYEE_ID")
    private Employee employee;

    @Column(name = "ORDER_DATE", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "TOTAL_AMOUNT", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "FINAL_AMOUNT", nullable = false, precision = 18, scale = 2)
    private BigDecimal finalAmount;

    @Column(name = "PAYMENT_METHOD", nullable = false, length = 30)
    private String paymentMethod;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Column(name = "NOTE", length = 500)
    private String note;
@OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalesOrderDetail> details = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        initAuditTimestamps();
        LocalDateTime now = LocalDateTime.now();
        if (orderDate == null) orderDate = now;
        if (status == null) status = "DRAFT";
        if (paymentMethod == null) paymentMethod = "CASH";
    }
}

