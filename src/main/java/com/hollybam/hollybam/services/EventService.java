package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_EventDao;
import com.hollybam.hollybam.dto.EventDetailDto;
import com.hollybam.hollybam.dto.EventDto;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService implements IF_EventService {
    @Autowired
    private IF_EventDao eventDao;
    @Autowired
    private SqlSession sqlSession;

    @Override
    public int insertEvent(EventDto eventDto) {
        eventDao.insertEvent(eventDto);
        return eventDto.getEventCode(); // useGeneratedKeys=true로 채워진 값 반환
    }

    @Override
    public void insertEventDetail(EventDetailDto eventDetailDto) {
        eventDao.insertEventDetail(eventDetailDto);
    }

    @Override
    public List<EventDto> selectVisibleEvents() {
        return eventDao.selectVisibleEvents();
    }

    @Override
    public void updateEventStatuses() {
        sqlSession.update("com.hollybam.hollybam.dao.IF_EventDao.updateEventStatuses");
    }

    @Override
    public EventDto selectEventByCode(int eventCode) {
        return eventDao.selectEventByCode(eventCode);
    }

    @Override
    public EventDetailDto selectEventDetailByEventCode(int eventCode) {
        return eventDao.selectEventDetailByEventCode(eventCode);
    }
}
