package com.hollybam.hollybam.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {
    @GetMapping("/")
    public String introPage(){
        return "intro";
    }

    @GetMapping("/main")
    public String mainPage(){
        return "main";
    }

    @GetMapping("/loading")
    public String loadingPage(){ return "/loading"; }
}
