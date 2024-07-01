package com.example.diamondstore.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "[Order]")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderid")
    private Integer orderId;

    @ManyToOne
    @JoinColumn(name = "customerid")
    private User cid;

    @NotBlank(message = "Customer name is required")
    @Column(name = "customer_name", nullable = false)
    private String cname;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    @Column(name = "phone_number", nullable = false, unique = true)
    private String phone;

    @Column(name = "address")
    private String address;

    @Email(regexp = "^[a-zA-Z][a-zA-Z0-9._%+-]*@gmail\\.[a-zA-Z]{2,}$", message = "Email must be a valid Gmail address and should not start with a digit")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @PastOrPresent(message = "Order date must be in the past or present")
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @Column(name = "order_date")
    private Date order_date;

    @Column(name = "status")
    private String status;

    @ManyToOne
    @JoinColumn(name = "voucherid")
    private Voucher voucherId;

    @Column(name = "payment_amount")
    private BigDecimal payment;

    @Column(name = "payment_status")
    private boolean paymentStatus;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @Column(name = "payment_date")
    private Date payment_date;

    @Column(name = "payment_method")
    private String payment_method;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @Column(name = "delivery_date")
    private Date delivery;

    @ManyToOne
    @JoinColumn(name = "delivery_staffid")
    private User deliveryStaff;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @PrePersist
    @PreUpdate
    public void validateDates() {
        if (payment_date != null && order_date != null && payment_date.compareTo(order_date) < 0) {
            throw new IllegalArgumentException("Payment date must be after the order date");
        }
        if (delivery != null && order_date != null && delivery.before(order_date)) {
            throw new IllegalArgumentException("Delivery date must be after the order date");
        }
    }

    @JsonIgnore
    @OneToMany(mappedBy = "orderId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<OrderDetail> orderDetails;
}
