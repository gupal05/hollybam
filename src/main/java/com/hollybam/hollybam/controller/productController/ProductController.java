package com.hollybam.hollybam.controller.productController;

import com.hollybam.hollybam.dto.ImageDto;
import com.hollybam.hollybam.dto.ProductDto;
import com.hollybam.hollybam.dto.ProductOptionDto;
import com.hollybam.hollybam.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping("/{productId}")
    public ModelAndView getProductPage(@PathVariable String productId, ModelAndView mav) {
        ProductDto productDto = new ProductDto();
        productDto.setProductId(productId);
        int productCode = productService.getProductCode(productDto);

        List<ImageDto> thumbnails = productService.getProductInfoThumbnail(productCode);
        List<ImageDto> contents = productService.getProductInfoContent(productCode);
        List<ProductOptionDto> options = productService.getProductOptions(productCode);

        // 옵션 이름들을 Set으로 추출
        Set<String> optionNames = options.stream()
                .map(ProductOptionDto::getOptionName)
                .collect(Collectors.toSet());

        mav.addObject("thumbnails", thumbnails);
        mav.addObject("contents", contents);
        mav.addObject("product", productService.getProductDetailInfo_first(productId));
        mav.addObject("options", options);
        mav.addObject("optionNames", optionNames); // 추가
        mav.setViewName("/product/productDetail");
        return mav;
    }


}
