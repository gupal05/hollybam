package com.hollybam.hollybam.services.admin;

import com.hollybam.hollybam.dao.admin.IF_AdminBannerDao;
import com.hollybam.hollybam.util.S3Uploader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AdminBannerServiceImpl implements IF_AdminBannerService{
    @Autowired
    private IF_AdminBannerDao adminBannerDao;
    @Autowired
    private S3Uploader s3Uploader;

    @Override
    @Transactional
    public List<Map<String,Object>> getBannerList(){
        return adminBannerDao.getBannerList();
    }

    public void uploadBanner(MultipartFile[] files, String[] clickEvents) throws IOException {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            String clickEvent = (clickEvents.length > i) ? clickEvents[i] : null;

            if (!file.isEmpty()) {
                System.out.println(file);
                System.out.println(clickEvent);
                String url = s3Uploader.upload(file, "banner");
                map.put("url", url);
                map.put("event", clickEvent);
                map.put("order", i);
                adminBannerDao.insBanner(map);
            }
        }
    }
}
