package com.example.diamondstore.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "DiamondMount")
public class DiamondMount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mountid")
    private int mountId;

    @Column(name = "mount_name", nullable = false)
    private String mountName;

    @Column(name = "size", nullable = false)
    private float size;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "material", nullable = false)
    private String material;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @JsonIgnore
    @OneToMany(mappedBy = "mountId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<Product> products;
}
