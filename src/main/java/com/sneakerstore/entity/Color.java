package com.sneakerstore.entity;

import com.sneakerstore.entity.BaseEntity;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "COLORS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Color extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_colors")
    @SequenceGenerator(name = "seq_colors", sequenceName = "SEQ_COLORS", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "COLOR_CODE", nullable = false, unique = true, length = 30)
    private String colorCode;

    @Column(name = "COLOR_NAME", nullable = false, unique = true, length = 100)
    private String colorName;

    @Column(name = "HEX_CODE", nullable = false, length = 20)
    private String hexCode;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;
@PrePersist
    public void prePersist() {
        initAuditTimestamps();
        if (status == null) status = "ACTIVE";
    }
}

