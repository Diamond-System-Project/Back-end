package com.example.diamondstore.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "Product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "productid")
    private int productId;

    @Column(name = "productname")
    private String productName;

    @Column(name = "description", nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "mountid", nullable = false)
    private DiamondMount mountId;

    @Column(name = "components_price")
    private BigDecimal componentsPrice;

    @Column(name = "labor_fee")
    private BigDecimal laborFee;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "status")
    private String status;

    @JsonIgnore
    @OneToMany(mappedBy = "productId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.Collection<ProductDiamond> productDiamonds;

    @JsonIgnore
    @OneToMany(mappedBy = "productId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.Collection<Comment> comments;

    @JsonIgnore
    @OneToMany(mappedBy = "productId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.Collection<OrderDetail> orderDetails;

    @JsonIgnore
    @OneToMany(mappedBy = "productId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.Collection<ProductPrice> productPrices;

    @JsonIgnore
    @OneToMany(mappedBy = "productId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.Collection<ProductPromotion> productPromotions;

    @JsonIgnore
    @OneToMany(mappedBy = "productId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.Collection<Inventory> inventories;

    @JsonIgnore
    @OneToMany(mappedBy = "productId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.Collection<Image> images;

    @JsonIgnore
    @OneToMany(mappedBy = "productId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.Collection<CollectionProducts> collectionProducts;
}
