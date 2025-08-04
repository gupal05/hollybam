package com.hollybam.hollybam.scheduler;

import com.hollybam.hollybam.services.admin.AdminSpecialSaleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class SpecialSaleScheduler {

    @Autowired
    private AdminSpecialSaleService adminSpecialSaleService;

    /**
     * 매일 자정에 특가 상품 상태 업데이트 (월별 관리 방식)
     * 현재 월의 특가 상품들만 활성화하고 다른 월의 특가 상품들은 비활성화
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    public void updateDailySpecialSales() {
        try {
            log.info("월별 특가 상품 상태 업데이트 시작 (매일 체크)");

            Map<String, Integer> result = adminSpecialSaleService.updateSpecialSaleStatusForScheduler();

            log.info("월별 특가 상품 상태 업데이트 완료 - 결과: {}", result);

        } catch (Exception e) {
            log.error("월별 특가 상품 상태 업데이트 중 예외 발생", e);
        }
    }

    /**
     * 테스트용 - 매분마다 실행 (개발/테스트 시에만 사용)
     * 운영 환경에서는 반드시 주석 처리 또는 삭제
     */
//     @Scheduled(cron = "0 * * * * *") // 매분 실행 (테스트용)
    public void testSpecialSaleUpdate() {
        try {
            log.info("테스트: 특가 상품 상태 업데이트");
            Map<String, Integer> result = adminSpecialSaleService.updateSpecialSaleStatusForScheduler();
            log.info("테스트 결과: {}", result);
        } catch (Exception e) {
            log.error("테스트 실행 중 오류", e);
        }
    }
}