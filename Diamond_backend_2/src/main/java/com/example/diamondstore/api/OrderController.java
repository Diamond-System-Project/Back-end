package com.example.diamondstore.api;

import com.example.diamondstore.dto.*;
import com.example.diamondstore.entities.*;
import com.example.diamondstore.response.ApiResponse;
import com.example.diamondstore.services.interfaces.*;
import com.example.diamondstore.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/api/order")
@Validated
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductService productService;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private VoucherService voucherService;


    @PostMapping("/createWithDetails")
    @PreAuthorize("hasRole('ROLE_Member')")
    public ResponseEntity<ApiResponse> createOrderWithDetails(@Valid @RequestBody CreateOrderRequestDTO createOrderRequestDTO, HttpServletRequest request) {
        try {
            String authorizationHeader = request.getHeader("Authorization");
            Integer userId = null;

            if(authorizationHeader != null) {
                String token = authorizationHeader.substring(7);
                userId = jwtUtil.extractUserId(token);
            }

            if (createOrderRequestDTO.getOrder() == null) {
                return ResponseEntity.ok(ApiResponse.builder()
                        .success(false)
                        .message("Order data is missing")
                        .build());
            }

            if (createOrderRequestDTO.getOrderDetails() == null || createOrderRequestDTO.getOrderDetails().isEmpty()) {
                return ResponseEntity.ok(ApiResponse.builder()
                        .success(false)
                        .message("Order details are missing")
                        .build());
            }

            if(createOrderRequestDTO.getOrder().getVoucherId() != null && userId != null) {
                Voucher voucher = voucherService.getVoucherById(createOrderRequestDTO.getOrder().getVoucherId());
                if(voucher != null && userId != voucher.getMemberId().getUserId()){
                    return ResponseEntity.ok(ApiResponse.builder()
                            .success(false)
                            .message("Voucher Not Belongs To This Member")
                            .build());
                }
                else if (voucher != null && !voucher.getStatus().equals("Active")) {
                    return ResponseEntity.ok(ApiResponse.builder()
                            .success(false)
                            .message("Voucher has been Used or Expired")
                            .build());
                }
            }

            boolean checkQuantity = true;
            List<OrderDetailDTO> orderDetails = createOrderRequestDTO.getOrderDetails();
            for (OrderDetailDTO detailDTO : orderDetails) {

                Product product = productService.getProductById(detailDTO.getProductId());
                List<Inventory> inventoryList = inventoryService.getListByProductAndAvailable(product, true);
                int totalQuantityInventory = 0;

                if (!inventoryList.isEmpty()) {
                    for (Inventory i : inventoryList) {
                        totalQuantityInventory += i.getQuantity();
                    }
                }

                if (inventoryList.isEmpty() || detailDTO.getQuantity() > totalQuantityInventory || detailDTO.getQuantity() <= 0) {
                    checkQuantity = false;
                }
            }

            if (!checkQuantity) {
                return ResponseEntity.ok(ApiResponse.builder()
                        .success(false)
                        .message("Failed to create order and order details: Product not In Stock or Out of Stock")
                        .build());
            } else {

                orderService.createOrderAndDetails(createOrderRequestDTO, userId);
                return ResponseEntity.ok(ApiResponse.builder()
                        .success(true)
                        .message("Order and order details created successfully")
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("Failed to create order and order details: " + e.getMessage())
                    .build());
        }
    }


    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse> getOrderId(@PathVariable Integer orderId) {
        try {
            Order order = orderService.getOrderId(orderId);
            if (order != null) {
                return ResponseEntity.ok(ApiResponse.builder()
                        .success(true)
                        .message("Get Order by Id Success~")
                        .data(order)
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.builder()
                                .success(false)
                                .message("Can't find any order with id = " + orderId)
                                .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.builder()
                            .success(false)
                            .message("Failed to get order: " + e.getMessage())
                            .build());
        }
    }

    //ko gắn
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createOrder(@Valid @RequestBody OrderDTO orderDTO) {
        try {
            Order saveOrder = orderService.createOrder(orderDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.builder()
                            .success(true)
                            .message("Order Created Successfully")
                            .data(saveOrder)
                            .build());
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ApiResponse.builder()
                            .success(false)
                            .message("Failed to create order due to data integrity violation: " + e.getMessage())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.builder()
                            .success(false)
                            .message("Failed to create order: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ROLE_Manager', 'ROLE_Sales Staff')")
    public ResponseEntity<ApiResponse> getAllOrders() {
        try {
            List<Order> orders = orderService.getAllOrder();
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("List of Orders:")
                    .data(orders)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.builder()
                            .success(false)
                            .message("Failed to list orders: " + e.getMessage())
                            .build());
        }
    }

    @PutMapping("/update/{orderId}")
    @PreAuthorize("hasAnyRole('ROLE_Member', 'ROLE_Manager')")
    public ResponseEntity<ApiResponse> updateOrder(@Valid @RequestBody UpdateOrderDTO updateOrderDTO, @PathVariable Integer orderId) {
        try {
            Order existingOrder = orderService.getOrderId(orderId);
            if (existingOrder == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.builder()
                                .success(false)
                                .message("Update failed: Can't find this order")
                                .build());
            }

            Order updatedOrder = orderService.updateOrderByMember(updateOrderDTO, orderId);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Order Updated Successfully!")
                    .data(updatedOrder)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.builder()
                            .success(false)
                            .message("Update failed: " + e.getMessage())
                            .build());
        }
    }

    //khoan gắn
    @DeleteMapping("/delete/{orderId}")
    public ResponseEntity<ApiResponse> deleteOrder(@PathVariable int orderId) {
        try {
            orderService.deleteOrder(orderId);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Order Deleted Successfully")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.builder()
                            .success(false)
                            .message("Failed to delete order: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_Manager', 'ROLE_Sales Staff')")
    public ResponseEntity<ApiResponse> getOrdersByUserId(@PathVariable Integer userId) {
        try {
            if (userService.getUserById(userId) == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        ApiResponse.builder()
                                .success(false)
                                .message("User Not Found")
                                .build());
            }
            List<Order> orders = orderService.getOrdersByUserId(userId);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("List of Orders by User ID:")
                    .data(orders)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.builder()
                            .success(false)
                            .message("Failed to list orders: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/history/{userId}")
    @PreAuthorize("hasRole('ROLE_Member')")
    public ResponseEntity<ApiResponse> getOrdersByCurrentUserId(@PathVariable Integer userId, HttpServletRequest request) {
        try {
            if (userService.getUserById(userId) == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        ApiResponse.builder()
                                .success(false)
                                .message("User Not Found")
                                .build());
            }
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                int currentUserId = jwtUtil.extractUserId(token);
                if (userId != currentUserId) {
                    return ResponseEntity.ok(ApiResponse.builder()
                            .success(false)
                            .message("Does not have permission to view this user!")
                            .build());
                }
            }
            List<Order> orders = orderService.getOrdersByUserId(userId);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("List of Orders by User ID:")
                    .data(orders)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.builder()
                            .success(false)
                            .message("Failed to list orders: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/delivery/{id}")
    @PreAuthorize("hasRole('ROLE_Delivery Staff')")
    public ResponseEntity<ApiResponse> getOrdersByDeliveryStaffId(@PathVariable Integer id) {
        try {
            List<Order> orders = orderService.getOrdersByDeliveryStaffId(id);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("List of Orders by Delivery Staff ID:")
                    .data(orders)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.builder()
                            .success(false)
                            .message("Failed to list orders: " + e.getMessage())
                            .build());
        }
    }

    @PutMapping("/assign")
    @PreAuthorize("hasRole('ROLE_Manager')")
    public ResponseEntity<ApiResponse> assignOrder(@RequestBody DeliveryDTO deliveryDTO) {
        try {
            Order existingOrder = orderService.getOrderId(deliveryDTO.getOrderId());
            User delivery = userService.getUserById(deliveryDTO.getDeliveryId());
            if (existingOrder == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.builder()
                                .success(false)
                                .message("Assign failed: Can't find this order")
                                .build());
            } else if (delivery == null || delivery.getRoleid().getRoleid() != 4) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.builder()
                                .success(false)
                                .message("Assign failed: Invalid Delivery Staff")
                                .build());
            } else if (!(existingOrder.getStatus().equals("Pending") || existingOrder.getStatus().equals("Processing"))) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.builder()
                                .success(false)
                                .message("Assign failed: Can't change status of this Order")
                                .build());
            } else {
                Order assignOrder = orderService.assignOrderToDelivery(deliveryDTO);
                return ResponseEntity.ok(ApiResponse.builder()
                        .success(true)
                        .message("Order Assigned Successfully!")
                        .data(assignOrder)
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.builder()
                            .success(false)
                            .message("Assign failed: " + e.getMessage())
                            .build());
        }
    }

    @PutMapping("/delivery/status")
    @PreAuthorize("hasRole('ROLE_Delivery Staff')")
    public ResponseEntity<ApiResponse> updateOrderStatusByDelivery(@RequestBody UpdateOrderStatusDTO updateOrderStatusDTO, HttpServletRequest request) {
        try {
            Order existingOrder = orderService.getOrderId(updateOrderStatusDTO.getOrderId());
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                int userId = jwtUtil.extractUserId(token);
                if(userId != existingOrder.getDeliveryStaff().getUserId()){
                    return ResponseEntity.ok(ApiResponse.builder()
                            .success(false)
                            .message("Does not have permission to update this order!")
                            .build());
                }
            }
            if (existingOrder == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.builder()
                                .success(false)
                                .message("Update failed: Can't find this order")
                                .build());
            } else if (existingOrder.getStatus().equals("Delivered")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.builder()
                                .success(false)
                                .message("Order has been delivered. Can't not set status!")
                                .build());
            } else if (existingOrder.getStatus().equals(updateOrderStatusDTO.getStatus())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.builder()
                                .success(false)
                                .message("Can't not set the same status")
                                .build());
            } else if ((existingOrder.getStatus().equals("Processing") && updateOrderStatusDTO.getStatus().equals("Shipping"))
                    || (existingOrder.getStatus().equals("Shipping") && updateOrderStatusDTO.getStatus().equals("Delivered"))) {
                Order order = orderService.updateOrderStatusByDelivery(updateOrderStatusDTO);
                return ResponseEntity.ok(ApiResponse.builder()
                        .success(true)
                        .message("Order Updated Successfully!")
                        .data(order)
                        .build());
            } else {
                return ResponseEntity.ok(ApiResponse.builder()
                        .success(false)
                        .message("Invalid Status")
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.builder()
                            .success(false)
                            .message("Update failed: " + e.getMessage())
                            .build());
        }
    }

    @PutMapping("/cancel/{id}")
    @PreAuthorize("hasRole('ROLE_Manager')")
    public ResponseEntity<ApiResponse> cancelOrder(@PathVariable int id, @RequestBody CancelOrderDTO cancelOrderDTO) {
        try {
            Order order = orderService.getOrderId(id);
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.builder()
                                .success(false)
                                .message("Cancel failed: Can't find this order")
                                .build());
            } else if (order.getStatus().equals("Delivered") || order.getStatus().equals("Cancelled")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.builder()
                                .success(false)
                                .message("Order has been Delivered or Cancelled!")
                                .build());
            } else if (cancelOrderDTO.getReason().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.builder()
                                .success(false)
                                .message("Reason can not be empty!")
                                .build());
            } else {
                Order cancelOrder = orderService.cancelOrder(id, cancelOrderDTO);
                return ResponseEntity.ok(ApiResponse.builder()
                        .success(true)
                        .message("Cancel Order Successfully")
                        .data(cancelOrder)
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.builder()
                            .success(false)
                            .message("Cancel failed: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/list/deliveryOrder")
    @PreAuthorize("hasRole('ROLE_Manager')")
    public ResponseEntity<ApiResponse> getDeliveryShippingOrderNumber() {
        try {
            List<DeliveryShippingOrderCountDTO> list = orderService.getDeliveryShippingOrderNumber();
            if (list.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.builder()
                        .success(false)
                        .message("No Delivery Staff Found!")
                        .build());
            } else {
                return ResponseEntity.ok(ApiResponse.builder()
                                .success(true)
                                .message("List Delivery Staff and Their Orders:")
                                .data(list)
                                .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.builder()
                            .success(false)
                            .message("Failed to get order: " + e.getMessage())
                            .build());
        }
    }
}

