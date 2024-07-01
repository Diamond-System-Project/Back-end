package com.example.diamondstore.services;

import com.example.diamondstore.dto.ProductPromotionDTO;
import com.example.diamondstore.entities.Product;
import com.example.diamondstore.entities.ProductPrice;
import com.example.diamondstore.entities.ProductPromotion;
import com.example.diamondstore.entities.Promotion;
import com.example.diamondstore.repositories.ProductPriceRepository;
import com.example.diamondstore.repositories.ProductPromotionRepository;
import com.example.diamondstore.repositories.ProductRepository;
import com.example.diamondstore.repositories.PromotionRepository;
import com.example.diamondstore.services.interfaces.ProductPromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ProductPromotionServiceImpl implements ProductPromotionService {
    @Autowired
    private ProductPromotionRepository productPromotionRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private PromotionRepository promotionRepository;
    @Autowired
    private ProductPriceRepository productPriceRepository;

    @Override
    public List<ProductPromotion> productPromotionList() {
        return productPromotionRepository.findAll();
    }

    @Override
    public ProductPromotion getProductPromotionById(int id) {
        return productPromotionRepository.findByProductPromotionId(id);
    }

    @Override
    public List<ProductPromotion> getListByProductId(int id) {
        return productPromotionRepository.findByProductId(productRepository.findProductByProductId(id));
    }

    @Override
    public List<ProductPromotion> getListByPromotionId(int id) {
        return productPromotionRepository.findByPromotionId(promotionRepository.findPromotionByPromotionId(id));
    }

    @Override
    public void createProductPromotion(ProductPromotionDTO productPromotionDTO) {
        Promotion promotion = promotionRepository.findPromotionByPromotionId(productPromotionDTO.getPromotionId());
        List<Integer> list = productPromotionDTO.getProductIds();

        for(Integer pId : list){
            if(productPromotionRepository.findProductPromotionByPromotionIdAndProductId(
                    promotion, productRepository.findProductByProductId(pId)) == null)
                productPromotionRepository.save(ProductPromotion.builder()
                        .promotionId(promotion)
                        .productId(productRepository.findProductByProductId(pId))
                        .discount(productPromotionDTO.getDiscount())
                        .startDate(productPromotionDTO.getStartDate())
                        .endDate(productPromotionDTO.getEndDate())
                        .isActive(false)
                        .build());
        }
    }

    @Override
    public boolean deleteProductPromotions(List<Integer> productPromotionIds) {
        try{
            for (Integer productPromotionId : productPromotionIds) {
                if (!productPromotionRepository.findByProductPromotionId(productPromotionId).isActive())
                    productPromotionRepository.deleteById(productPromotionId);
            }
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    @Override
    public void updateProductPromotion(ProductPromotionDTO productPromotionDTO) {
        Promotion promotion = promotionRepository.findPromotionByPromotionId(productPromotionDTO.getPromotionId());
        List<Integer> list = productPromotionDTO.getProductIds();

        for(Integer pId : list){
            ProductPromotion productPromotion = productPromotionRepository.findProductPromotionByPromotionIdAndProductId(
                    promotion, productRepository.findProductByProductId(pId));

            if(!productPromotion.isActive()){
                productPromotion.setDiscount(productPromotionDTO.getDiscount());
                productPromotion.setStartDate(productPromotionDTO.getStartDate());
                productPromotion.setEndDate(productPromotionDTO.getEndDate());
                productPromotionRepository.save(productPromotion);
            }
        }
    }

    @Override
    public void changeStatus(ProductPromotionDTO productPromotionDTO) {
        Promotion promotion = promotionRepository.findPromotionByPromotionId(productPromotionDTO.getPromotionId());
        List<Integer> list = productPromotionDTO.getProductIds();

        for(Integer pId : list){
            List<ProductPromotion> products = productPromotionRepository.findByProductId(productRepository.findProductByProductId(pId));
            for(ProductPromotion pp : products){
                if(pp.getPromotionId().getPromotionId() != promotion.getPromotionId())
                    pp.setActive(false);
            }

            ProductPromotion productPromotion = productPromotionRepository.findProductPromotionByPromotionIdAndProductId(
                    promotion, productRepository.findProductByProductId(pId));

            productPromotion.setActive(!productPromotion.isActive());
            productPromotionRepository.save(productPromotion);

            ProductPrice productPrice = productPriceRepository.findTop1ByProductIdOrderByUpdateDateDesc(
                    productRepository.findProductByProductId(pId));
            Product product = productRepository.findProductByProductId(pId);
            if(productPrice != null && productPromotion.isActive()){
                product.setPrice(productPrice.getSellingPrice().multiply(BigDecimal.valueOf(1 - productPromotion.getDiscount())));
                productRepository.save(product);
            }
            else {
                assert productPrice != null;
                product.setPrice(productPrice.getSellingPrice());
                productRepository.save(product);
            }
        }
    }
}
