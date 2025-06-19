package com.hollybam.hollybam.controller;

import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.dto.ProductDto;
import com.hollybam.hollybam.services.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class HomeController {
    @Autowired
    private ProductService productService;
    @Autowired
    private HttpSession session;

    @GetMapping("/")
    public String introPage(HttpServletRequest request, Model model) {
//        System.out.println(request.getServerName());
//        if(request.getServerName().equals("adult-high.local")){
//            System.out.println(1);
//        } else {
//            System.out.println(2);
//        }
        return "intro";
    }

    @GetMapping("/main")
    public ModelAndView mainPage(ModelAndView mav, RedirectAttributes rttr){
        List<ProductDto> proList = productService.selectBestProducts();
        List<ProductDto> newProList = productService.selectNewProducts();
        System.out.println(newProList.size());
        mav.addObject("proList", proList);
        mav.addObject("newProList", newProList);
        mav.setViewName("main");
        return mav;
    }

    @GetMapping("/loading")
    public String loadingPage(){ return "/loading"; }

    @GetMapping("/real")
    public String realPage(){ return "/real"; }
}
