package com.hollybam.hollybam.controller.adminController;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.hollybam.hollybam.controller.adminController")
public class GlobalAdminControllerAdvice {

    @ModelAttribute
    public void addCurrentPath(HttpServletRequest request, Model model) {
        model.addAttribute("currentPath", request.getRequestURI());
    }
}