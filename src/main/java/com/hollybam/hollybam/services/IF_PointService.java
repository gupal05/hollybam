package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.PointDto;

public interface IF_PointService {

    /**
     * 회원의 현재 적립금 조회
     * @param memberCode 회원 코드
     * @return 현재 적립금 잔액
     */
    int getCurrentPoints(int memberCode);

    /**
     * 적립금 사용
     * @param memCode 회원 코드
     * @param usePoints 사용할 적립금
     * @param orderCode 관련 주문 코드
     * @param description 사용 사유
     * @return 성공 여부
     */
    boolean usePoints(int memCode, int usePoints, int orderCode, String description);

    /**
     * 적립금 적립
     * @param memCode 회원 코드
     * @param savePoints 적립할 적립금
     * @param orderCode 관련 주문 코드
     * @param description 적립 사유
     * @return 성공 여부
     */
    boolean savePoints(int memCode, int savePoints, int orderCode, String description);

    /**
     * 구매 금액의 1.5% 적립금 계산
     * @param orderAmount 주문 금액
     * @return 적립될 적립금
     */
    int calculateRewardPoints(int orderAmount);
}