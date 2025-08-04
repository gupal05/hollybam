package com.hollybam.hollybam.dao.admin;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface IF_AdminSpecialSaleDao {
    List<Map<String, Object>> selectCategory();
    List<Map<String, Object>> selectCategoryDetail(@Param("categoryCode") String categoryCode);
    List<Map<String, Object>> searchProducts(@Param("cateCode") String cateCode, @Param("cdCode") String cdCode, @Param("keyword") String keyword);
    int selectOriginalPrice(@Param("productCode") int productCode);
    int insertSpecialSaleProducts(List<Map<String, Object>> products);
    List<Map<String, Object>> selectSpecialSaleList(@Param("startDate") String startDate);
}
