package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_GuestDao;
import com.hollybam.hollybam.dto.GuestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuestService implements IF_GuestService {
    @Autowired
    private IF_GuestDao guestDao;

    @Override
    public int isGuest(String guestDi) {
        return guestDao.isGuest(guestDi);
    }

    @Override
    public GuestDto getGuestByDi(String guestDi) {
        return guestDao.getGuestByDi(guestDi);
    }

    @Override
    @Transactional
    public void insertGuest(GuestDto guest) {
        guestDao.insertGuest(guest);
    }
}
