package com.example.diamondstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductDTO {
    private String productName;
    private String description;
    private int mountId;
    private BigDecimal laborFee;
    private String status;
}
