package com.sneakerstore.entity;

import com.sneakerstore.entity.BaseEntity;
import lombok.experimental.SuperBuilder;

import com.sneakerstore.entity.Employee;
import com.sneakerstore.entity.Supplier;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PURCHASE_ORDERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PurchaseOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_purchase_orders")
    @SequenceGenerator(name = "seq_purchase_orders", sequenceName = "SEQ_PURCHASE_ORDERS", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PURCHASE_CODE", nullable = false, unique = true, length = 30)
    private String purchaseCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SUPPLIER_ID", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMPLOYEE_ID")
    private Employee employee;

    @Column(name = "PURCHASE_DATE", nullable = false)
    private LocalDateTime purchaseDate;

    @Column(name = "TOTAL_AMOUNT", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Column(name = "NOTE", length = 500)
    private String note;

    @Column(name = "CONFIRMED_AT")
    private LocalDateTime confirmedAt;

    @Column(name = "CANCELLED_AT")
    private LocalDateTime cancelledAt;
@OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PurchaseOrderDetail> details = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        initAuditTimestamps();
        LocalDateTime now = LocalDateTime.now();
        if (purchaseDate == null) purchaseDate = now;
        if (status == null) status = "DRAFT";
    }
}

