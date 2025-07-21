package com.hollybam.hollybam.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface IF_SearchDao {
    int countByKeyword(String keyword);
    List<Map<String,Object>> searchProductsByPage(Map<String,Object> params);
}
