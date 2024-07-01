package com.example.diamondstore.services;

import com.example.diamondstore.dto.OrderDetailDTO;
import com.example.diamondstore.dto.UpdateOrderDetailDTO;
import com.example.diamondstore.entities.Order;
import com.example.diamondstore.entities.OrderDetail;
import com.example.diamondstore.entities.Product;
import com.example.diamondstore.exceptions.DataNotFoundException;
import com.example.diamondstore.repositories.OrderDetailRepository;
import com.example.diamondstore.repositories.OrderRepository;
import com.example.diamondstore.repositories.ProductRepository;
import com.example.diamondstore.services.interfaces.OrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderDetailServiceImpl implements OrderDetailService {


    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public OrderDetail createOrderDetail(OrderDetailDTO orderDetailDTO) {

        Product product = productRepository.findProductByProductId(orderDetailDTO.getProductId());
        OrderDetail saveOrderDetail = orderDetailRepository.save(OrderDetail.builder()
                        .productId(product)
                        .orderId(orderRepository.findByOrderId(orderDetailDTO.getOrderId()))
                        .quantity(orderDetailDTO.getQuantity())
                        .price(product.getPrice().multiply(BigDecimal.valueOf(orderDetailDTO.getQuantity())))
                .build());

        return saveOrderDetail;
    }

    @Override
    public OrderDetail updateOrderDetail(Integer orderDetailId, UpdateOrderDetailDTO updateOrderDetailDTO) throws DataNotFoundException {
        OrderDetail existingOrderDetail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new DataNotFoundException("Order detail not found"));

        Product product = productRepository.findProductByProductId(existingOrderDetail.getProductId().getProductId());
        existingOrderDetail.setQuantity(updateOrderDetailDTO.getQuantity());
        existingOrderDetail.setPrice(product.getPrice().multiply(BigDecimal.valueOf(updateOrderDetailDTO.getQuantity())));

        return orderDetailRepository.save(existingOrderDetail);
    }

    @Override
    public void deleteOrderDetail(Integer orderDetailId) throws DataNotFoundException {
        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new DataNotFoundException("Order detail not found"));
        orderDetailRepository.delete(orderDetail);
    }

    @Override
    public OrderDetail getOrderDetailById(Integer orderDetailId) throws DataNotFoundException {
        return orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new DataNotFoundException("Order detail not found"));
    }

    @Override
    public List<OrderDetail> getAllOrderDetail() {

        return orderDetailRepository.findAll();
    }

    @Override
    public List<OrderDetail> getOrderDetailsByOrderId(Integer orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        return orderDetailRepository.findByOrderId(order);
    }

}
