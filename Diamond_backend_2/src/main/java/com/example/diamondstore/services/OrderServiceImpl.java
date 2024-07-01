
package com.example.diamondstore.services;

import com.example.diamondstore.dto.*;
import com.example.diamondstore.entities.*;
import com.example.diamondstore.repositories.*;
import com.example.diamondstore.services.interfaces.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private VoucherRepository voucherRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void createOrderAndDetails(CreateOrderRequestDTO createOrderRequestDTO, Integer userId) {
        Order order = new Order();
        order.setCname(createOrderRequestDTO.getOrder().getCname());
        order.setPhone(createOrderRequestDTO.getOrder().getPhone());
        order.setEmail(createOrderRequestDTO.getOrder().getEmail());
        order.setAddress(createOrderRequestDTO.getOrder().getAddress());
        order.setPayment_method(createOrderRequestDTO.getOrder().getPayment_method());
        order.setStatus("Pending");
        order.setPaymentStatus(false);
        if(userId != null){
            User user = userRepository.findUserByUserId(userId);
            order.setCid(user);
            order.setEmail(user.getEmail());
        }

        Voucher voucher;
        if(createOrderRequestDTO.getOrder().getVoucherId() != null){
            voucher = voucherRepository.findByVoucherId(createOrderRequestDTO.getOrder().getVoucherId());
            order.setVoucherId(voucher);
            voucher.setStatus("Used");
            voucherRepository.save(voucher);
        }

        Order savedOrder = orderRepository.save(order);
        float total = 0.0f;

        List<OrderDetailDTO> orderDetails = createOrderRequestDTO.getOrderDetails();
        for (OrderDetailDTO detailDTO : orderDetails) {

            Product product = productRepository.findProductByProductId(detailDTO.getProductId());

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(savedOrder);
            orderDetail.setProductId(product);
            orderDetail.setQuantity(detailDTO.getQuantity());
            orderDetail.setPrice(productRepository.findProductByProductId(detailDTO.getProductId()).getPrice());
            orderDetailRepository.save(orderDetail);

            total += detailDTO.getQuantity() * productRepository.findProductByProductId(detailDTO.getProductId()).getPrice();

            List<Inventory> inventoryList = inventoryRepository.findByProductIdAndAvailable(product, true);
            int totalQuantity = detailDTO.getQuantity();
            for(Inventory inventory : inventoryList){
                if(inventory.getQuantity() > totalQuantity){
                    inventory.setQuantity(inventory.getQuantity() - totalQuantity);
                    totalQuantity = 0;
                }
                else if (inventory.getQuantity() < totalQuantity) {
                    totalQuantity = totalQuantity - inventory.getQuantity();
                    inventory.setQuantity(0);
                }
                inventoryRepository.save(inventory);
            }
        }

        if(savedOrder.getVoucherId() != null){
            voucher = voucherRepository.findByVoucherId(savedOrder.getVoucherId().getVoucherId());
            savedOrder.setPayment(total * (1 - voucher.getDiscount()));
        }
        else savedOrder.setPayment(total);
        orderRepository.save(savedOrder);
        //return savedOrder;
    }

    @Override
    public Order createOrder(OrderDTO orderDTO) {
        Order saveOrder = orderRepository.save(Order.builder()
                .cname(orderDTO.getCname())
                        .phone(orderDTO.getPhone())
                .email(orderDTO.getEmail())
                .address(orderDTO.getAddress())
                .payment_method(orderDTO.getPayment_method())
                        .status("Pending")
                .build());

        return saveOrder;
    }

    @Override
    public List<Order> getAllOrder() {
        return orderRepository.findAll();
    }

    @Override
    public void deleteOrder(Integer orderId) {
        orderRepository.deleteById(orderId);
    }

    @Override
    public Order getOrderId(Integer orderId) {
        return orderRepository.findByOrderId(orderId);
    }

    @Override
    public Order updateOrderByMember(UpdateOrderDTO updateOrderDTO, Integer orderId) {
        Order saveOrder = orderRepository.findByOrderId(orderId);
        saveOrder.setCname(updateOrderDTO.getCname());
        saveOrder.setAddress(updateOrderDTO.getAddress());
        saveOrder.setEmail(updateOrderDTO.getEmail());
        saveOrder.setPhone(updateOrderDTO.getPhone());
        saveOrder.setPayment_method(updateOrderDTO.getPayment_method());

        return  orderRepository.save(saveOrder);
    }

    @Override
    public List<Order> getOrdersByUserId(Integer userId) {
        return orderRepository.findByCid(userRepository.findUserByUserId(userId));
    }

    @Override
    public List<Order> getOrdersByDeliveryStaffId(Integer deliveryId) {
        return orderRepository.findByDeliveryStaff(userRepository.findUserByUserId(deliveryId));
    }

    @Override
    public Order assignOrderToDelivery(DeliveryDTO deliveryDTO) {
        Order order = orderRepository.findByOrderId(deliveryDTO.getOrderId());
        order.setDeliveryStaff(userRepository.findUserByUserId(deliveryDTO.getDeliveryId()));
        order.setStatus("Processing");
        return orderRepository.save(order);
    }

    @Transactional
    @Override
    public Order updateOrderStatusByDelivery(UpdateOrderStatusDTO updateOrderStatusDTO) {
        Order order = orderRepository.findByOrderId(updateOrderStatusDTO.getOrderId());
        if(updateOrderStatusDTO.getStatus().equals("Delivered")){
            order.setStatus(updateOrderStatusDTO.getStatus());
            order.setDelivery(Date.from(Instant.now()));
            if(order.getPayment_method().equals("COD")){
                order.setPayment_date(Date.from(Instant.now()));
                order.setPaymentStatus(true);
            }
            if(order.getCid() != null){
                User user = order.getCid();
                userRepository.updatePointByUserId(user.getPoint() + Math.round(order.getPayment()/10000), user.getUserId());
            }
        }
        else order.setStatus(updateOrderStatusDTO.getStatus());
        return orderRepository.save(order);
    }

    @Override
    public Order cancelOrder(int orderId, CancelOrderDTO cancelOrderDTO) {
        Order order = orderRepository.findByOrderId(orderId);
        order.setStatus("Cancelled");
        order.setCancelReason(cancelOrderDTO.getReason());

        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order);
        for (OrderDetail orderDetail : orderDetails) {
            Product product = productRepository.findProductByProductId(orderDetail.getProductId().getProductId());
            Inventory inventory = inventoryRepository.findTop1ByProductId(product);
            inventory.setQuantity(inventory.getQuantity() + orderDetail.getQuantity());
            inventoryRepository.save(inventory);
        }

        if(order.getVoucherId() != null) {
            Voucher voucher = voucherRepository.findByVoucherId(order.getVoucherId().getVoucherId());
            voucher.setStatus("Active");
            voucherRepository.save(voucher);
        }
        return orderRepository.save(order);
    }

    @Override
    public List<DeliveryShippingOrderCountDTO> getDeliveryShippingOrderNumber() {
        List<DeliveryShippingOrderCountDTO> list = new ArrayList<>();
        List<User> listDelivery = userRepository.findUsersByRoleid(roleRepository.findRoleByRoleid(4));

        for(User d : listDelivery){
            DeliveryShippingOrderCountDTO dto = new DeliveryShippingOrderCountDTO();
            dto.setDeliveryId(d.getUserId());
            dto.setDeliveryName(d.getFullName());
            dto.setNumberOfOrders(orderRepository.countShippingOrderByDeliveryId(d));
            list.add(dto);
        }
        return list;
    }
}
