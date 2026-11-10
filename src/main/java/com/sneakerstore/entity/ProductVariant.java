package com.sneakerstore.entity;

import com.sneakerstore.entity.BaseEntity;
import lombok.experimental.SuperBuilder;

import com.sneakerstore.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRODUCT_VARIANTS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProductVariant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_product_variants")
    @SequenceGenerator(name = "seq_product_variants", sequenceName = "SEQ_PRODUCT_VARIANTS", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SIZE_ID", nullable = false)
    private ShoeSize size;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COLOR_ID", nullable = false)
    private Color color;

    @Column(name = "SKU", nullable = false, unique = true, length = 60)
    private String sku;

    @Column(name = "IMPORT_PRICE", nullable = false, precision = 18, scale = 2)
    private BigDecimal importPrice;

    @Column(name = "SALE_PRICE", nullable = false, precision = 18, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "AVAILABLE_QUANTITY", nullable = false)
    private Integer availableQuantity;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;
@PrePersist
    public void prePersist() {
        initAuditTimestamps();
        if (status == null) status = "ACTIVE";
        if (availableQuantity == null) availableQuantity = 0;
    }
}

