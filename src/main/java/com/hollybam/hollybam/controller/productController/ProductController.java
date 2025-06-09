package com.hollybam.hollybam.controller.productController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/product")
public class ProductController {
    @GetMapping("/productDetail")
    public String productDetailPage(){
        return "/product/productDetail";
    }
}
