package com.example.diamondstore.repositories;

import com.example.diamondstore.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    Product findProductByProductId(int id);
    @Query(value = "select p.* from [Product] p where p.[productname] like %?1%", nativeQuery = true)
    List<Product> findProductByProductName(String name);
}
