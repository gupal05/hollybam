package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.EventDetailDto;
import com.hollybam.hollybam.dto.EventDto;

import java.util.List;

public interface IF_EventService {
    int insertEvent(EventDto eventDto);
    void insertEventDetail(EventDetailDto eventDetailDto);
    List<EventDto> selectVisibleEvents();
    void updateEventStatuses();
    EventDto selectEventByCode(int eventCode);
    EventDetailDto selectEventDetailByEventCode(int eventCode);
}
