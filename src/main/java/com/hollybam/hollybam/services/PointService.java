package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_PointDao;
import com.hollybam.hollybam.dto.PointDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PointService implements IF_PointService {

    @Autowired
    private IF_PointDao pointDao;

    @Override
    public int getCurrentPoints(int memberCode) {
        try {
            Integer points = pointDao.getCurrentPoints(memberCode);
            return points != null ? points : 0;
        } catch (Exception e) {
            log.error("적립금 조회 중 오류 발생 - memCode: {}", memberCode, e);
            return 0;
        }
    }

    @Override
    @Transactional
    public boolean usePoints(int memberCode, int usePoints, int orderCode, String description) {
        try {
            // 현재 적립금 확인
            int currentPoints = getCurrentPoints(memberCode);
            if (currentPoints < usePoints) {
                log.warn("적립금 부족 - memCode: {}, 보유: {}, 사용요청: {}", memberCode, currentPoints, usePoints);
                return false;
            }

            // 적립금 사용 로그 생성
            PointDto pointDto = new PointDto();
            pointDto.setMemberCode(memberCode);
            pointDto.setPointChange(-usePoints); // 음수로 사용 표시
            pointDto.setPointType("USE");
            pointDto.setDescription(description);
            pointDto.setRelatedOrderCode(orderCode);

            int result = pointDao.insertPointLog(pointDto);

            if (result > 0) {
                log.info("적립금 사용 완료 - memCode: {}, 사용금액: {}, 주문: {}", memberCode, usePoints, orderCode);
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("적립금 사용 중 오류 발생 - memCode: {}, usePoints: {}", memberCode, usePoints, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean savePoints(int memCode, int savePoints, int orderCode, String description) {
        try {
            // 적립금 적립 로그 생성
            PointDto pointDto = new PointDto();
            pointDto.setMemberCode(memCode);
            pointDto.setPointChange(savePoints); // 양수로 적립 표시
            pointDto.setPointType("SAVE");
            pointDto.setDescription(description);
            pointDto.setRelatedOrderCode(orderCode);

            int result = pointDao.insertPointLog(pointDto);

            if (result > 0) {
                log.info("적립금 적립 완료 - memCode: {}, 적립금액: {}, 주문: {}", memCode, savePoints, orderCode);
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("적립금 적립 중 오류 발생 - memCode: {}, savePoints: {}", memCode, savePoints, e);
            return false;
        }
    }

    @Override
    public int calculateRewardPoints(int orderAmount) {
        // 구매 금액의 1.5% 계산 (소수점 이하 버림)
        double rewardRate = 0.015; // 1.5%
        int rewardPoints = (int) Math.floor(orderAmount * rewardRate);

        log.debug("적립금 계산 - 주문금액: {}, 적립금: {}", orderAmount, rewardPoints);
        return rewardPoints;
    }
}