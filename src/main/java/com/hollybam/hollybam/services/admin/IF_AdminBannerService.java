package com.hollybam.hollybam.services.admin;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public interface IF_AdminBannerService {
    List<Map<String,Object>> getBannerList();
    int deleteBanner(@RequestParam("bannerCode") int bannerCode) throws URISyntaxException;
}
