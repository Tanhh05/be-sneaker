package com.sneakerstore.entity;

import com.sneakerstore.entity.BaseEntity;
import lombok.experimental.SuperBuilder;

import com.sneakerstore.entity.Brand;
import com.sneakerstore.entity.Category;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRODUCTS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_products")
    @SequenceGenerator(name = "seq_products", sequenceName = "SEQ_PRODUCTS", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PRODUCT_CODE", nullable = false, unique = true, length = 30)
    private String productCode;

    @Column(name = "PRODUCT_NAME", nullable = false, length = 200)
    private String productName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRAND_ID", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID", nullable = false)
    private Category category;

    @Column(name = "MATERIAL", length = 150)
    private String material;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "THUMBNAIL_URL", length = 500)
    private String thumbnailUrl;

    @Column(name = "IMPORT_PRICE", nullable = false, precision = 18, scale = 2)
    private BigDecimal importPrice;

    @Column(name = "SALE_PRICE", nullable = false, precision = 18, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;
@PrePersist
    public void prePersist() {
        initAuditTimestamps();
        if (status == null) status = "ACTIVE";
    }
}

