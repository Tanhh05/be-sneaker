package com.sneakerstore.entity;

import com.sneakerstore.entity.BaseEntity;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "SUPPLIERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Supplier extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_suppliers")
    @SequenceGenerator(name = "seq_suppliers", sequenceName = "SEQ_SUPPLIERS", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "SUPPLIER_CODE", nullable = false, unique = true, length = 30)
    private String supplierCode;

    @Column(name = "SUPPLIER_NAME", nullable = false, length = 200)
    private String supplierName;

    @Column(name = "CONTACT_PERSON", length = 100)
    private String contactPerson;

    @Column(name = "PHONE", nullable = false, length = 20)
    private String phone;

    @Column(name = "EMAIL", length = 100)
    private String email;

    @Column(name = "ADDRESS", length = 255)
    private String address;

    @Column(name = "TAX_CODE", length = 50)
    private String taxCode;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;
@PrePersist
    public void prePersist() {
        initAuditTimestamps();
        if (status == null) status = "ACTIVE";
    }
}

