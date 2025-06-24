package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.EventDetailDto;
import com.hollybam.hollybam.dto.EventDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IF_EventDao {
    int insertEvent(EventDto eventDto);
    int insertEventDetail(EventDetailDto eventDetailDto);
    List<EventDto> selectVisibleEvents(); // 진행중 또는 예정인 이벤트 조회
    void updateEventStatuses();
    EventDto selectEventByCode(int eventCode);
    EventDetailDto selectEventDetailByEventCode(int eventCode);
}