package com.hollybam.hollybam.services.admin;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IF_AdminSpecialSaleService {
    List<Map<String, Object>> selectCategory();
    List<Map<String, Object>> selectCategoryDetail(String categoryCode);
    List<Map<String, Object>> searchProducts(String cateCode, String cdCode, String keyword);
    int selectOriginalPrice(@Param("productCode") int productCode);
    int insertSpecialSaleProducts(List<Map<String, Object>> products);
    List<Map<String, Object>> selectSpecialSaleList(String startDate);
    Map<String, Integer> updateSpecialSaleStatusForScheduler();
}
