package com.sneakerstore.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "PRODUCT_REVIEWS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProductReview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_product_reviews")
    @SequenceGenerator(name = "seq_product_reviews", sequenceName = "SEQ_PRODUCT_REVIEWS", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SALES_ORDER_ID")
    private SalesOrder salesOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID")
    private Customer customer;

    @Column(name = "CUSTOMER_NAME", nullable = false, length = 100)
    private String customerName;

    @Column(name = "CUSTOMER_PHONE", length = 20)
    private String customerPhone;

    @Column(name = "RATING", nullable = false)
    private Integer rating;

    @Column(name = "FIT_FEEDBACK", length = 50)
    private String fitFeedback;

    @Column(name = "COMMENT_TEXT", length = 1000)
    private String commentText;

    @Column(name = "PHOTO_URLS", length = 2000)
    private String photoUrls;

    @Column(name = "IS_PURCHASED")
    @Builder.Default
    private Integer isPurchased = 1;

    @Column(name = "STATUS", length = 20)
    @Builder.Default
    private String status = "APPROVED";

    @PrePersist
    public void prePersist() {
        initAuditTimestamps();
        if (this.status == null) this.status = "APPROVED";
        if (this.isPurchased == null) this.isPurchased = 1;
        if (this.getCreatedBy() == null) this.setCreatedBy("CLIENT");
        if (this.getUpdatedBy() == null) this.setUpdatedBy("CLIENT");
    }
}
