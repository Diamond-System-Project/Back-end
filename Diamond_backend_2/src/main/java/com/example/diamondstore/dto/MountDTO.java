package com.example.diamondstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MountDTO {
    private String mountName;
    private float size;
    private String type;
    private String material;
    private BigDecimal basePrice;
}
