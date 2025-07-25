package com.hollybam.hollybam.services.admin;

import com.hollybam.hollybam.dao.admin.IF_AdminDashboardDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AdminDashboardServiceImpl implements IF_AdminDashboardService {
    @Autowired
    private IF_AdminDashboardDao adminDashboardDao;

    /**
     * 총 주문 수량 조회
     * @return 총 주문 수량
     */
    @Override
    @Transactional
    public int adminSelectOrderCount(){
        return adminDashboardDao.adminSelectOrderCount();
    }

    /**
     * 총 회원 수 조회
     * @return 총 회원 수(비회원 제외)
     */
    @Override
    @Transactional
    public int adminSelectMemberCount(){
        return adminDashboardDao.adminSelectMemberCount();
    }

    /**
     * 총 상품 수량 조회
     * @return 총 상품 수량
     */
    @Override
    @Transactional
    public int adminSelectProductCount(){
        return adminDashboardDao.adminSelectProductCount();
    }

    /**
     * 총 결제 금액 조회
     * @param startDate 조회를 시작할 날짜
     * @param endDate 조회를 끝낼 날짜
     * @return 입력한 날짜에 따른 결제 금액 총 합
     */
    @Override
    @Transactional
    public long adminSelectTotalPaymentAmount(LocalDate startDate, LocalDate endDate){
        return adminDashboardDao.adminSelectTotalPaymentAmount(startDate, endDate);
    }

    /**
     * 총 취소/환불 금액 조회
     * @param startDate 조회를 시작할 날짜
     * @param endDate 조회를 끝낼 날짜
     * @return 입력한 날짜에 따른 취소/환불 금액 총 합
     */
    @Override
    @Transactional
    public long adminSelectCancelAmount(LocalDate startDate, LocalDate endDate){
        return adminDashboardDao.adminSelectCancelAmount(startDate, endDate);
    }

    /**
     * 취소/환불을 고려한 총 결제 금액 조회
     * @param startDate 조회를 시작할 날짜
     * @param endDate 조회를 끝낼 날짜
     * @return 입력한 날짜에 따른 취소/환불을 고려한 결제 금액의 총 합
     */
    @Override
    @Transactional
    public long adminSelectSalesAmount(LocalDate startDate, LocalDate endDate){
        return adminDashboardDao.adminSelectSalesAmount(startDate, endDate);
    }

    /**
     * 순수익(할인 금액은 고려 X) 금액 조회
     * @param startDate 조회를 시작할 날짜
     * @param endDate 조회를 끝낼 날짜
     * @return 입력한 날짜에 따른 순수익(할인 금액 고려 X) 총 합
     */
    @Override
    @Transactional
    public long adminSelectMargin(LocalDate startDate, LocalDate endDate){
        return adminDashboardDao.adminSelectMargin(startDate, endDate);
    }

    /**
     * 매출 차트 데이터 조회 (기간에 따라 자동으로 일별/월별/연도별 결정)
     */
    @Override
    @Transactional
    public Map<String, Object> getSalesChartData(LocalDate startDate, LocalDate endDate) {
        log.info("매출 차트 데이터 조회 요청: {} ~ {}", startDate, endDate);

        // 기간 차이 계산
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);

        // 기간에 따른 자동 단위 결정
        String periodType = this.determineOptimalPeriod(daysBetween);

        List<Map<String, Object>> chartData;

        switch (periodType) {
            case "daily":
                log.info("일별 매출 차트 조회 ({}일간)", daysBetween);
                chartData = adminDashboardDao.adminSelectDailySalesChart(startDate, endDate);
                break;

            case "monthly":
                log.info("월별 매출 차트 조회 ({}일간)", daysBetween);
                chartData = adminDashboardDao.adminSelectMonthlySalesChart(startDate, endDate);
                break;

            case "yearly":
                log.info("연도별 매출 차트 조회 ({}일간)", daysBetween);
                chartData = adminDashboardDao.adminSelectYearlySalesChart(startDate, endDate);
                break;

            default:
                throw new IllegalArgumentException("Invalid period type: " + periodType);
        }

        log.info("차트 데이터 조회 완료: {} 타입, {} 개 데이터", periodType, chartData.size());

        // 응답 Map 구성
        Map<String, Object> response = new HashMap<>();
        response.put("chartData", chartData);
        response.put("periodType", periodType);
        response.put("startDate", startDate.toString());
        response.put("endDate", endDate.toString());
        response.put("totalDays", daysBetween);

        return response;
    }

    /**
     * 기간에 따른 최적 조회 단위 결정
     * @param daysBetween 날짜 차이
     * @return "daily", "monthly", "yearly"
     */
    private String determineOptimalPeriod(long daysBetween) {
        if (daysBetween <= 90) {
            return "daily";     // 90일 이하 → 일별
        } else if (daysBetween <= 731) {
            return "monthly";   // 2년 이하 → 월별
        } else {
            return "yearly";    // 2년 초과 → 연도별
        }
    }

    @Override
    @Transactional
    public int getPaymentStatusCount(String status){
        return adminDashboardDao.getPaymentStatusCount(status);
    }

    @Override
    @Transactional
    public int getOrderStatusCount(String status){
        return adminDashboardDao.getOrderStatusCount(status);
    }

    @Override
    @Transactional
    public int getDeliveryStatusCount(String status){
        return adminDashboardDao.getDeliveryStatusCount(status);
    }
}
