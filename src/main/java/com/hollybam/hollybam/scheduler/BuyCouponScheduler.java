package com.hollybam.hollybam.scheduler;

import com.hollybam.hollybam.services.CouponService;
import com.hollybam.hollybam.services.IF_CouponService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Component
public class BuyCouponScheduler {
    @Autowired
    private CouponService couponService;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정마다 실행
    public void updateEventStatuses() {
        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> map = couponService.getBuyCouponDate();
        for (Map<String, Object> map1 : map) {
            String issuedAt = (String) map1.get("issuedAt");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime issuedDateTime = LocalDateTime.parse(issuedAt, formatter);
            long days = ChronoUnit.DAYS.between(issuedDateTime, now);
            if (days >= 15) {
                couponService.deleteBuyCoupon(Integer.parseInt(map1.get("cmCode").toString()));
            }
        }
    }
}
