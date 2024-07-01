package com.example.diamondstore.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Collection;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "Diamond")
public class Diamond {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diamondid")
    private int diamondId;

    @Column(name = "diamond_name", nullable = false)
    private String diamondName;

    @Column(name = "origin", nullable = false)
    private String origin;

    @Column(name = "carat_weight")
    private float caratWeight;

    @Column(name = "color")
    private String color;

    @Column(name = "clarity")
    private String clarity;

    @Column(name = "cut")
    private String cut;

    @Column(name = "shape", nullable = false)
    private String shape;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @JsonIgnore
    @OneToMany(mappedBy = "diamondId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<ProductDiamond> productDiamonds;

    @JsonIgnore
    @OneToOne(mappedBy = "diamondId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Certificate certificate;
}
