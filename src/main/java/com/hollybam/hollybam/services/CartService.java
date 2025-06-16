package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_CartDao;
import com.hollybam.hollybam.dto.CartDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService implements IF_CartService {
    private final IF_CartDao cartDao;

    public CartService(IF_CartDao cartDao) {
        this.cartDao = cartDao;
    }

    @Override
    @Transactional
    public int addToCart(CartDto cartDto) {
        try {
            // 기존 아이템이 있는지 확인
            CartDto existingItem = cartDao.findExistingCartItem(cartDto);
            if (existingItem != null) {
                // 기존 아이템이 있으면 수량만 증가
                return cartDao.updateCartQuantity(existingItem.getCartCode(),
                        existingItem.getQuantity() + cartDto.getQuantity());
            } else {
                // 새로운 아이템 추가
                return cartDao.addToCart(cartDto);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    @Transactional
    public List<CartDto> getCartItemsByMember(int memCode) {
        return cartDao.getCartItemsByMember(memCode);
    }

    @Override
    @Transactional
    public List<CartDto> getCartItemsByGuest(String guestUuid) {
        return cartDao.getCartItemsByGuest(guestUuid);
    }

    @Override
    @Transactional
    public int updateCartQuantity(int cartCode, int quantity) {
        return cartDao.updateCartQuantity(cartCode, quantity);
    }

    @Override
    @Transactional
    public int removeFromCart(int cartCode) {
        return cartDao.removeFromCart(cartCode);
    }

    @Override
    @Transactional
    public int clearCartByMember(int memCode) {
        return cartDao.clearCartByMember(memCode);
    }

    @Override
    @Transactional
    public int clearCartByGuest(String guestUuid) {
        return cartDao.clearCartByGuest(guestUuid);
    }

    @Override
    @Transactional
    public int getGuestCodeByUuid(String guestUuid) {
        return cartDao.getGuestCodeByUuid(guestUuid);
    }

    // 추가 메서드들 구현
    @Override
    @Transactional
    public CartDto getCartOwner(int cartCode) {
        return cartDao.getCartOwner(cartCode);
    }

    @Override
    @Transactional
    public int updateCartSelection(int cartCode, boolean selected) {
        return cartDao.updateCartSelection(cartCode, selected);
    }

    @Override
    @Transactional
    public int removeMultipleFromCart(List<Integer> cartCodes) {
        if (cartCodes == null || cartCodes.isEmpty()) {
            return 0;
        }
        return cartDao.removeMultipleFromCart(cartCodes);
    }
}
