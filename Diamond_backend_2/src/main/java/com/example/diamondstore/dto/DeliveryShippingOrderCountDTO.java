package com.example.diamondstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DeliveryShippingOrderCountDTO {
    private Integer deliveryId;
    private String deliveryName;
    private Integer numberOfOrders;
}
