package com.hollybam.hollybam.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String introPage(){
        return "intro";
    }

    @PostMapping("/main")
    public String mainPage(){
        return "main";
    }

    @GetMapping("/product/productDetail")
    public String productDetailPage(){
        return "/product/productDetail";
    }

    @GetMapping("/cate-page")
    public String catePage(){
        return "/categories";
    }
}
