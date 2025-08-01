package com.hollybam.hollybam.services.admin;

import org.apache.ibatis.annotations.Param;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IF_AdminPopupService {
    boolean insertPopup(@Param("popupImage") MultipartFile popupImage, @Param("popupDescription") String popupDescription, @Param("popupUrl") String popupUrl) throws IOException;
    List<Map<String, Object>> getPopupList();
    int getPopupCount();
}
