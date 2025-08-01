package com.hollybam.hollybam.dao.admin;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Mapper
public interface IF_AdminBannerDao {
    List<Map<String,Object>> getBannerList();
    void insBanner(Map<String,Object> map);
    int getLastBannerOrder();
    int getBannerOrder(@Param("bannerCode") int bannerCode);
    int deleteBanner(@Param("bannerCode") int bannerCode);
    String selectBannerUrl(@Param("bannerCode") int bannerCode);
    int decrementOrdersAbove(@Param("oldOrder") int oldOrder);
}
