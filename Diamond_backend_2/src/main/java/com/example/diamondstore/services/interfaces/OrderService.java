package com.example.diamondstore.services.interfaces;

import com.example.diamondstore.dto.*;
import com.example.diamondstore.entities.Order;
import com.example.diamondstore.entities.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface OrderService {
    void createOrderAndDetails(CreateOrderRequestDTO createOrderRequestDTO, Integer userId);
    Order createOrder(OrderDTO orderDTO);
    List<Order> getAllOrder();
    void deleteOrder(Integer orderId);
    Order getOrderId(Integer orderId);
    Order updateOrderByMember(UpdateOrderDTO updateOrderDTO, Integer orderId);
    List<Order> getOrdersByUserId(Integer userId);
    List<Order> getOrdersByDeliveryStaffId(Integer deliveryId);
    Order assignOrderToDelivery(DeliveryDTO deliveryDTO);
    Order updateOrderStatusByDelivery(UpdateOrderStatusDTO updateOrderStatusDTO);
    Order cancelOrder(int orderId, CancelOrderDTO cancelOrderDTO);
    List<DeliveryShippingOrderCountDTO> getDeliveryShippingOrderNumber();
}
