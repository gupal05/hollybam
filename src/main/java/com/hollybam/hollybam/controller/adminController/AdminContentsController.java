package com.hollybam.hollybam.controller.adminController;

import com.hollybam.hollybam.services.admin.AdminBannerServiceImpl;
import com.hollybam.hollybam.services.admin.AdminPopupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminContentsController {
    @Autowired
    private AdminBannerServiceImpl adminBannerServiceImpl;
    @Autowired
    private AdminPopupService adminPopupServiceImpl;

    @GetMapping("/banner/list")
    public String bannerList(Model model){
        List<Map<String,Object>> bannerList = adminBannerServiceImpl.getBannerList();
        model.addAttribute("bannerList",bannerList);
        return "admin/contents/createBanner";
    }

    @PostMapping("/banner/upload")
    public String uploadMultipleBanners(@RequestParam("bannerImages") MultipartFile[] files,
                                        @RequestParam("bannerClickEvents") String[] clickEvents) throws IOException {
        adminBannerServiceImpl.uploadBanner(files, clickEvents);
        return "redirect:/admin/banner/list";
    }

    @PostMapping("/banner/delete")
    @ResponseBody
    public Map<String, Object> deleteBanner(@RequestParam("bannerCode") int bannerCode, Model model) throws URISyntaxException {
        Map<String,Object> data = new HashMap<>();
        if(adminBannerServiceImpl.deleteBanner(bannerCode) > 0){
            data.put("success", true);
        } else {
            data.put("success", false);
            data.put("message", "네트워크 오류가 발생했습니다.\n잠시후에 다시 시도해주세요.");
        }
        return data;
    }

    @GetMapping("/create/popup")
    public String createPopup(Model model){
        return "admin/contents/createPopup";
    }

    @PostMapping("/popup/create")
    @ResponseBody
    public Map<String, Object> createPopup(@RequestParam("popup_image") MultipartFile popup_image, Model model, @RequestParam("popup_description") String popup_description, @RequestParam("popup_url") String popup_url) throws IOException {
        boolean ok = adminPopupServiceImpl.insertPopup(popup_image, popup_description, popup_url);

        Map<String, Object> res = new HashMap<>();
        res.put("result", ok);
        if (!ok) {
            res.put("message", "DB에 저장 실패");
        }
        return res;
    }
}
