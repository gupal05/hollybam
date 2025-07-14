package com.hollybam.hollybam.dao.admin;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface IF_AdminBannerDao {
    List<Map<String,Object>> getBannerList();
    void insBanner(Map<String,Object> map);
}
