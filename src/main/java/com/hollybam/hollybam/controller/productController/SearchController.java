package com.hollybam.hollybam.controller.productController;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/search")
public class SearchController {
    @GetMapping
    public String search(Model model) {
        return "search";
    }
}
