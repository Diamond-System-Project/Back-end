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
@Table(name = "OrderDetail")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detailid")
    private Integer orderDetailId;

    @ManyToOne
    @JoinColumn(name = "orderid", nullable = false)
    private Order orderId;

    @ManyToOne
    @JoinColumn(name = "productid", nullable = false)
    private Product productId;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price")
    private BigDecimal price;

    @JsonIgnore
    @OneToMany(mappedBy = "orderDetailId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<Warranty> warranties;
}
