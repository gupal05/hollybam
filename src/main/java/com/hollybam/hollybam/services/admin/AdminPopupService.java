package com.hollybam.hollybam.services.admin;

import com.hollybam.hollybam.dao.admin.IF_AdminPopupDao;
import com.hollybam.hollybam.util.S3Uploader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AdminPopupService implements IF_AdminPopupService {
    @Autowired
    IF_AdminPopupDao popupDao;
    @Autowired
    S3Uploader s3Uploader;

    @Override
    @Transactional
    public boolean insertPopup(MultipartFile imageFile, String description, String targetUrl) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("popupImage", imageFile.getOriginalFilename());
        map.put("popupDescription", description);
        map.put("popupUrl", targetUrl);
        int rowsAffected = popupDao.insertPopup(map);

        if (rowsAffected <= 0) {
            return false;
        }

        s3Uploader.upload(imageFile, "popup");
        return true;
    }

    @Override
    @Transactional
    public List<Map<String, Object>> getPopupList(){
        return popupDao.getPopupList();
    }

    @Override
    @Transactional
    public int getPopupCount(){
        return popupDao.getPopupCount();
    }
}
