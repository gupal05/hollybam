package com.hollybam.hollybam.scheduler;

import com.hollybam.hollybam.services.IF_EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EventStatusScheduler {

    @Autowired
    private IF_EventService eventService;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정마다 실행
    public void updateEventStatuses() {
        eventService.updateEventStatuses();
    }
}
