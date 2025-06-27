package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.GuestDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IF_GuestDao {
    public int isGuest(String guestDi);
    public GuestDto getGuestByDi(String guestDi);
    public void insertGuest(GuestDto guest);
}
