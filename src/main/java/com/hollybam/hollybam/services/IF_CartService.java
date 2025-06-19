package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.CartDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IF_CartService {
    public int addToCart(CartDto cartDto);
    public List<CartDto> getCartItemsByMember(int memCode);
    public List<CartDto> getCartItemsByGuest(String guestUuid);
    public int updateCartQuantity(int cartCode, int quantity);
    public int removeFromCart(int cartCode);
    public int clearCartByMember(int memCode);
    public int clearCartByGuest(String guestUuid);
    public int getGuestCodeByUuid(String guestUuid);

    // 추가 메서드들
    public CartDto getCartOwner(int cartCode);
    public int updateCartSelection(int cartCode, boolean selected);
    public int removeMultipleFromCart(List<Integer> cartCodes);
}