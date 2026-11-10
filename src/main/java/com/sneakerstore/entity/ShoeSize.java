package com.sneakerstore.entity;

import com.sneakerstore.entity.BaseEntity;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "SIZES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ShoeSize extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_sizes")
    @SequenceGenerator(name = "seq_sizes", sequenceName = "SEQ_SIZES", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "SIZE_VALUE", nullable = false, unique = true, length = 20)
    private String sizeValue;

    @Column(name = "SIZE_NUMBER", nullable = false, precision = 5, scale = 1)
    private BigDecimal sizeNumber;

    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;
@PrePersist
    public void prePersist() {
        initAuditTimestamps();
        if (status == null) status = "ACTIVE";
    }
}

