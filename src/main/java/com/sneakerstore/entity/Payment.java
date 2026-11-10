package com.sneakerstore.entity;

import com.sneakerstore.entity.BaseEntity;
import lombok.experimental.SuperBuilder;

import com.sneakerstore.entity.SalesOrder;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PAYMENTS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_payments")
    @SequenceGenerator(name = "seq_payments", sequenceName = "SEQ_PAYMENTS", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PAYMENT_CODE", nullable = false, unique = true, length = 30)
    private String paymentCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SALES_ORDER_ID", nullable = false)
    private SalesOrder salesOrder;

    @Column(name = "PAYMENT_METHOD", nullable = false, length = 30)
    private String paymentMethod;

    @Column(name = "AMOUNT", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "PAYMENT_DATE", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Column(name = "REFERENCE_NOTE", length = 255)
    private String referenceNote;
@PrePersist
    public void prePersist() {
        initAuditTimestamps();
        LocalDateTime now = LocalDateTime.now();
        if (paymentDate == null) paymentDate = now;
        if (status == null) status = "PAID";
    }
}

