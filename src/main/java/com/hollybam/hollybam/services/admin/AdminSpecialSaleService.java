package com.hollybam.hollybam.services.admin;

import com.hollybam.hollybam.dao.admin.IF_AdminSpecialSaleDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
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
    /**
     * 현재 월 기준으로 특가 상품 활성화/비활성화
     * start_date의 년월이 현재 년월과 같은 특가만 활성화
     */
    @Override
    @Transactional
    public Map<String, Integer> updateSpecialSaleStatusForScheduler() {
        Map<String, Integer> result = new HashMap<>();
        LocalDate now = LocalDate.now();

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("year", now.getYear());
            params.put("month", now.getMonthValue());

            // 현재 월과 같은 월의 특가 상품 활성화
            int activatedCount = adminSpecialSaleDao.activateCurrentMonthSpecialSales(params);
            // 현재 월이 아닌 특가 상품 비활성화
            int deactivatedCount = adminSpecialSaleDao.deactivateNotCurrentMonthSpecialSales(params);

            result.put("activated", activatedCount);
            result.put("deactivated", deactivatedCount);
            result.put("totalActive", adminSpecialSaleDao.countActiveSpecialSales());

            log.info("{}년 {}월 특가 상품 상태 업데이트 완료 - 활성화: {}, 비활성화: {}, 총 활성화: {}",
                    now.getYear(), now.getMonthValue(), activatedCount, deactivatedCount, result.get("totalActive"));

        } catch (Exception e) {
            log.error("월별 특가 상품 상태 업데이트 중 오류 발생", e);
            throw e;
        }

        return result;
    }

}
