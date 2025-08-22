package com.hollybam.hollybam.controller.hollybamPickController;

import com.hollybam.hollybam.dto.ProductDto;
import com.hollybam.hollybam.services.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/new")
public class NewProductController {
    @Autowired
    private ProductService productService;

    @GetMapping
    public String hollybamPick(Model model) {
        List<ProductDto> newProList = productService.selectNewProductsAll();
        for(int i = 0; i < newProList.size(); i++){
            newProList.get(i).setWishCount(productService.getWishCount(newProList.get(i).getProductCode()));
            if(productService.isSpecialSale(newProList.get(i).getProductCode()) > 0){
                newProList.get(i).setSale(true);
                newProList.get(i).setSalePrice(productService.getProductDetailSalePrice(newProList.get(i).getProductCode()));

                int originalPrice = newProList.get(i).getPriceDtoList().get(0).getPriceOriginal();
                int salePrice = newProList.get(i).getSalePrice();

                int discountRate = 0;
                if(originalPrice > 0){
                    discountRate = (int) Math.round(((originalPrice - salePrice) / (double) originalPrice) * 100);
                }
                newProList.get(i).setSpecialDiscountRate(discountRate);
            }
            newProList.get(i).getPriceDtoList().get(0).setPercentage((newProList.get(i).getPriceDtoList().get(0).getPriceOriginal() - newProList.get(i).getPriceDtoList().get(0).getPriceSelling()) * 100 / newProList.get(i).getPriceDtoList().get(0).getPriceOriginal());
        }
        model.addAttribute("proList", newProList);
        return "newProduct";
    }
}
