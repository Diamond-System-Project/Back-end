package com.example.diamondstore.repositories;
import com.example.diamondstore.entities.Order;
import com.example.diamondstore.entities.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    Order findByOrderId(Integer orderId);

    List<Order> findByCid(User cid);

    List<Order> findByDeliveryStaff(User delivery);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status NOT IN ('Cancelled', 'Delivered')")
    int countProcessingOrder(); // Ngoai tru cac status la Cancel, Deliver thi dem het

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'Delivered'")
    int countCompleteOrder(); //Dem Status Deliver


    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'Cancelled'")
    int countCancelOrder(); // Dem Cancel status

    @Query("SELECT SUM(o.payment) FROM Order o WHERE o.status = 'Delivered'")
    float totalRevenue(); // Dung ham Sum nhung Order co Status la Deliver

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'Shipping' AND o.deliveryStaff = :deliveryStaff")
    int countShippingOrderByDeliveryId(@Param("deliveryStaff") User deliveryStaff);
}