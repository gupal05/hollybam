package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.GuestDto;

public interface IF_GuestService {
    public int isGuest(String guestDi);
    public GuestDto getGuestByDi(String guestDi);
    public void insertGuest(GuestDto guest);
}
