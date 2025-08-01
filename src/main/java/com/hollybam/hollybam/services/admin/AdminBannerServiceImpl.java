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
import java.net.URI;
import java.net.URISyntaxException;
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

    @Transactional
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
                adminBannerDao.insBanner(map);
            }
        }
    }

    @Override
    @Transactional
    public int deleteBanner(@RequestParam("bannerCode") int bannerCode) throws URISyntaxException {
        String bannerUrl = adminBannerDao.selectBannerUrl(bannerCode);
        System.out.println(bannerUrl);
        int oldOrder = adminBannerDao.getBannerOrder(bannerCode);
        int rows = adminBannerDao.deleteBanner(bannerCode);
        if (rows > 0 && bannerUrl != null) {
            // 3) URL에서 S3 키(버킷 뒤 경로) 추출
            //    예: https://bucket.s3.region.amazonaws.com/dir/filename.jpg
            URI uri = new URI(bannerUrl);
            String key = uri.getPath().substring(1);
            // 4) S3에서 파일 삭제
            s3Uploader.delete(key);
            adminBannerDao.decrementOrdersAbove(oldOrder);
        }
        return rows;
    }
}
