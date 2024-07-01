package com.example.diamondstore.services;

import com.example.diamondstore.dto.ProductPriceDTO;
import com.example.diamondstore.entities.Product;
import com.example.diamondstore.entities.ProductPrice;
import com.example.diamondstore.repositories.ProductPriceRepository;
import com.example.diamondstore.repositories.ProductRepository;
import com.example.diamondstore.services.interfaces.ProductPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class ProductPriceServiceImpl implements ProductPriceService {
    @Autowired
    private ProductPriceRepository productPriceRepository;
    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<ProductPrice> productPriceList() {
        return productPriceRepository.findAll();
    }

    @Override
    public ProductPrice getProductPriceById(int id) {
        return productPriceRepository.findByProductPriceId(id);
    }

    @Override
    public List<ProductPrice> getProductPriceListByProductId(int productId) {
        return productPriceRepository.findByProductId(
                productRepository.findProductByProductId(productId));
    }

    @Override
    public ProductPrice createProductPrice(ProductPriceDTO productPriceDTO) {
        Product product = productRepository.findProductByProductId(productPriceDTO.getProductId());
        ProductPrice saveProductPrice = productPriceRepository.save(ProductPrice.builder()
                .productId(product)
                .markupRate(productPriceDTO.getMarkupRate())
                .costPrice(product.getComponentsPrice().add(product.getLaborFee()))
                .sellingPrice((product.getComponentsPrice().add(product.getLaborFee())).multiply(BigDecimal.valueOf(productPriceDTO.getMarkupRate())))
                .updateDate(Date.from(Instant.now()))
                .build());

        product.setPrice(saveProductPrice.getSellingPrice());
        productRepository.save(product);
        return saveProductPrice;
    }

    @Override
    public void createProductPriceAuto(int productId) {
        Product product = productRepository.findProductByProductId(productId);
        ProductPrice productPrice = productPriceRepository.findTop1ByProductIdOrderByUpdateDateDesc(
                productRepository.findProductByProductId(productId));

        if(productPrice == null) return;

        if(productPrice.getCostPrice() != (product.getComponentsPrice().add(product.getLaborFee()))){
            ProductPrice saveProductPrice = productPriceRepository.save(ProductPrice.builder()
                    .productId(product)
                    .markupRate(productPrice.getMarkupRate())
                    .costPrice(product.getComponentsPrice().add(product.getLaborFee()))
                    .sellingPrice((product.getComponentsPrice().add(product.getLaborFee())).multiply(BigDecimal.valueOf(productPrice.getMarkupRate())))
                    .updateDate(Date.from(Instant.now()))
                    .build());

            product.setPrice(saveProductPrice.getSellingPrice().setScale(2, RoundingMode.HALF_UP));
            productRepository.save(product);
        }
    }

    @Override
    public boolean deleteProductPrices(List<Integer> productPriceIds) {
        try{
            for (Integer productPriceId : productPriceIds) {
                productPriceRepository.deleteById(productPriceId);
            }
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
}
