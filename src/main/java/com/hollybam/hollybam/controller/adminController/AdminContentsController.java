package com.hollybam.hollybam.controller.adminController;

import com.hollybam.hollybam.services.admin.AdminBannerServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/admin/banner")
public class AdminContentsController {
    @Autowired
    private AdminBannerServiceImpl adminBannerServiceImpl;

    @GetMapping("/list")
    public String bannerList(Model model){
        List<Map<String,Object>> bannerList = adminBannerServiceImpl.getBannerList();
        model.addAttribute("bannerList",bannerList);
        return  "admin/contents/banner";
    }

    @PostMapping("/upload")
    public String uploadMultipleBanners(@RequestParam("bannerImages") MultipartFile[] files,
                                        @RequestParam("bannerClickEvents") String[] clickEvents) throws IOException {
        adminBannerServiceImpl.uploadBanner(files, clickEvents);
        return "redirect:/admin/banner/list";
    }
}
