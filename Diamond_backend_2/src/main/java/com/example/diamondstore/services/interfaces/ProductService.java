package com.example.diamondstore.services.interfaces;

import com.example.diamondstore.dto.ProductDTO;
import com.example.diamondstore.entities.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public interface ProductService {
    List<Product> productList();
    Product getProductById(int id);
    Product createProduct(ProductDTO productDTO);
    Product updateProduct(ProductDTO productDTO, int id);
    List<Product> findProductByProductName(String name);
    BigDecimal calculateComponentsPrice(int productId);
    List<Product> getProductsByMountType(String type);
}
