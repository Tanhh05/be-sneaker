package com.sneakerstore.entity;

import com.sneakerstore.entity.BaseEntity;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ORDER_STATUS_HISTORY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderStatusHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_order_status_history")
    @SequenceGenerator(name = "seq_order_status_history", sequenceName = "SEQ_ORDER_STATUS_HISTORY", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SALES_ORDER_ID", nullable = false)
    private SalesOrder salesOrder;

    @Column(name = "FROM_STATUS", length = 20)
    private String fromStatus;

    @Column(name = "TO_STATUS", nullable = false, length = 20)
    private String toStatus;

    @Column(name = "NOTE", length = 500)
    private String note;

    @Column(name = "CHANGED_AT", nullable = false)
    private LocalDateTime changedAt;
@PrePersist
    public void prePersist() {
        initAuditTimestamps();
        LocalDateTime now = LocalDateTime.now();
        if (changedAt == null) changedAt = now;
    }
}

