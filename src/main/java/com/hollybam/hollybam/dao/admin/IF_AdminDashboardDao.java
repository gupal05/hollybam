package com.hollybam.hollybam.dao.admin;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface IF_AdminDashboardDao {
    /**
     * 총 주문 수량 조회
     * @return 총 주문 수량
     */
    int adminSelectOrderCount();

    /**
     * 총 회원 수 조회
     * @return 총 회원 수(비회원 제외)
     */
    int adminSelectMemberCount();

    /**
     * 총 상품 수량 조회
     * @return 총 상품 수량
     */
    int adminSelectProductCount();

    /**
     * 총 결제 금액 조회
     * @param startDate 조회를 시작할 날짜
     * @param endDate 조회를 끝낼 날짜
     * @return 입력한 날짜에 따른 결제 금액 총 합
     */
    long adminSelectTotalPaymentAmount(LocalDate startDate, LocalDate endDate);

    /**
     * 총 취소/환불 금액 조회
     * @param startDate 조회를 시작할 날짜
     * @param endDate 조회를 끝낼 날짜
     * @return 입력한 날짜에 따른 취소/환불 금액 총 합
     */
    long adminSelectCancelAmount(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 취소/환불을 고려한 총 결제 금액 조회
     * @param startDate 조회를 시작할 날짜
     * @param endDate 조회를 끝낼 날짜
     * @return 입력한 날짜에 따른 취소/환불을 고려한 결제 금액의 총 합
     */
    long adminSelectSalesAmount(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 순수익(할인 금액은 고려 X) 금액 조회
     * @param startDate 조회를 시작할 날짜
     * @param endDate 조회를 끝낼 날짜
     * @return 입력한 날짜에 따른 순수익(할인 금액 고려 X) 총 합
     */
    long adminSelectMargin(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 일별 매출 차트 데이터 조회
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 일별 매출 데이터 리스트 [{"period": "2024-01-01", "salesAmount": 1200000}, ...]
     */
    List<Map<String, Object>> adminSelectDailySalesChart(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 월별 매출 차트 데이터 조회
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 월별 매출 데이터 리스트 [{"period": "2024-01", "salesAmount": 35000000}, ...]
     */
    List<Map<String, Object>> adminSelectMonthlySalesChart( @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 연도별 매출 차트 데이터 조회
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 연도별 매출 데이터 리스트 [{"period": "2024", "salesAmount": 420000000}, ...]
     */
    List<Map<String, Object>> adminSelectYearlySalesChart(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    int getPaymentStatusCount(@Param("status") String status);
    int getOrderStatusCount(@Param("status") String status);
    int getDeliveryStatusCount(@Param("status") String status);
    List<Map<String, Object>> getDescOrder();
}
