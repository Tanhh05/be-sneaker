package com.sneakerstore.entity;

import com.sneakerstore.entity.BaseEntity;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "BRANDS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Brand extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_brands")
    @SequenceGenerator(name = "seq_brands", sequenceName = "SEQ_BRANDS", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BRAND_CODE", nullable = false, unique = true, length = 30)
    private String brandCode;

    @Column(name = "BRAND_NAME", nullable = false, unique = true, length = 100)
    private String brandName;

    @Column(name = "COUNTRY", length = 100)
    private String country;

    @Column(name = "LOGO_URL", length = 500)
    private String logoUrl;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;
@PrePersist
    public void prePersist() {
        initAuditTimestamps();
        if (status == null) status = "ACTIVE";
    }
}

