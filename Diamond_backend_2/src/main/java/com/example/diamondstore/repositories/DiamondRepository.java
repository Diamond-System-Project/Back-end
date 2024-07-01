package com.example.diamondstore.repositories;

import com.example.diamondstore.entities.Diamond;
import com.example.diamondstore.entities.DiamondMount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DiamondRepository extends JpaRepository<Diamond, Integer> {
    Diamond findDiamondByDiamondId(int id);

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Diamond d WHERE d.diamondName = ?1")
    boolean existsByDiamondName(String diamondName);

    @Query(value = "SELECT TOP 20 base_price FROM Diamond WHERE carat_weight = :caratWeight", nativeQuery = true)
    List<BigDecimal> findTop20PriceByCaratWeight(@Param("caratWeight") float caratWeight);
}
