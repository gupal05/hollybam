package com.hollybam.hollybam.services.admin;

import com.hollybam.hollybam.dao.admin.IF_AdminSpecialSaleDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AdminSpecialSaleService implements IF_AdminSpecialSaleService {
    @Autowired
    IF_AdminSpecialSaleDao adminSpecialSaleDao;

    @Override
    @Transactional
    public List<Map<String, Object>> selectCategory(){
        return adminSpecialSaleDao.selectCategory();
    }

    @Override
    @Transactional
    public List<Map<String, Object>> selectCategoryDetail(String categoryCode){
        return adminSpecialSaleDao.selectCategoryDetail(categoryCode);
    }

    @Override
    @Transactional
    public List<Map<String, Object>> searchProducts(String cateCode, String cdCode, String keyword){
        return adminSpecialSaleDao.searchProducts(cateCode,cdCode,keyword);
    }

    @Override
    @Transactional
    public int selectOriginalPrice(@Param("productCode") int productCode){
        return adminSpecialSaleDao.selectOriginalPrice(productCode);
    }

    @Override
    @Transactional
    public int insertSpecialSaleProducts(List<Map<String, Object>> products){
        return adminSpecialSaleDao.insertSpecialSaleProducts(products);
    }

    @Override
    @Transactional
    public List<Map<String, Object>> selectSpecialSaleList(String startDate){
        return adminSpecialSaleDao.selectSpecialSaleList(startDate);
    }

}
