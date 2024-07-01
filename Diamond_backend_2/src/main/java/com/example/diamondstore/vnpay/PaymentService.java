package com.example.diamondstore.vnpay;

import com.example.diamondstore.core.config.payment.VNPAYConfig;
import com.example.diamondstore.entities.Order;
import com.example.diamondstore.repositories.OrderRepository;
import com.example.diamondstore.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private final VNPAYConfig vnPayConfig;
    @Autowired
    private OrderRepository orderRepository;

    public PaymentDTO.VNPayResponse createVnPayPayment(HttpServletRequest request) {
        try {
            int orderId = Integer.parseInt(request.getParameter("orderId"));
            Order order = orderRepository.findByOrderId(orderId);
            if (order == null) {
                throw new IllegalArgumentException("Order not found");
            }
            // Số tiền phải là số nguyên đại diện cho số tiền tính bằng đồng Việt Nam (VND)
            long amount = Math.round(order.getPayment().doubleValue()) * 100L;
            String bankCode = request.getParameter("bankCode");

            logger.info(request.getHeader("Returnurl"));
            Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig(request.getHeader("Returnurl"));
            vnpParamsMap.put("vnp_Amount", String.valueOf(amount));
            vnpParamsMap.put("vnp_OrderInfo", String.valueOf(orderId));

            if (bankCode != null && !bankCode.isEmpty()) {
                vnpParamsMap.put("vnp_BankCode", bankCode);
            }
            vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));

            // Tạo URL yêu cầu
            String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
            String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
            String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
            queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
            String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;

            // Log URL cuối cùng để kiểm tra lỗi
            logger.info("Payment URL: {}", paymentUrl);

            return PaymentDTO.VNPayResponse.builder()
                    .code("ok")
                    .message("success")
                    .paymentUrl(paymentUrl)
                    .build();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid order ID", e);
        } catch (Exception e) {
            logger.error("Error creating VNPay payment", e);
            throw new RuntimeException("Error creating VNPay payment", e);
        }
    }

}