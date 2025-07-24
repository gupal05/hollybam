package com.hollybam.hollybam.services.admin;

import java.time.LocalDate;
import java.util.Map;

public interface IF_AdminDashboardService {
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
    long adminSelectCancelAmount(LocalDate startDate, LocalDate endDate);

    /**
     * 취소/환불을 고려한 총 결제 금액 조회
     * @param startDate 조회를 시작할 날짜
     * @param endDate 조회를 끝낼 날짜
     * @return 입력한 날짜에 따른 취소/환불을 고려한 결제 금액의 총 합
     */
    long adminSelectSalesAmount(LocalDate startDate, LocalDate endDate);

    /**
     * 순수익(할인 금액은 고려 X) 금액 조회
     * @param startDate 조회를 시작할 날짜
     * @param endDate 조회를 끝낼 날짜
     * @return 입력한 날짜에 따른 순수익(할인 금액 고려 X) 총 합
     */
    long adminSelectMargin(LocalDate startDate, LocalDate endDate);

    /**
     * 매출 차트 데이터 조회 (기간에 따라 자동으로 일별/월별/연도별 결정)
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 매출 차트 응답 데이터
     * {
     *   "chartData": [{"period": "2024-01-01", "salesAmount": 1200000}, ...],
     *   "periodType": "daily",
     *   "startDate": "2024-01-01",
     *   "endDate": "2024-03-31"
     * }
     */
    Map<String, Object> getSalesChartData(LocalDate startDate, LocalDate endDate);
}
