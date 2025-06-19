package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.CartDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IF_CartDao {
    public int addToCart(CartDto cartDto);
    public List<CartDto> getCartItemsByMember(int memCode);
    public List<CartDto> getCartItemsByGuest(String guestUuid);
    public int updateCartQuantity(@Param("cartCode") int cartCode, @Param("quantity") int quantity);
    public int removeFromCart(int cartCode);
    public int clearCartByMember(int memCode);
    public int clearCartByGuest(String guestUuid);
    public CartDto findExistingCartItem(CartDto cartDto);
    public int getGuestCodeByUuid(String guestUuid);
    // 추가 메서드: 장바구니 소유자 확인 (보안용)
    public CartDto getCartOwner(int cartCode);

    // 추가 메서드: 선택 상태 업데이트
    public int updateCartSelection(@Param("cartCode") int cartCode, @Param("selected") boolean selected);

    // 추가 메서드: 다중 삭제를 위한 IN 절 쿼리
    public int removeMultipleFromCart(@Param("cartCodes") List<Integer> cartCodes);
}
