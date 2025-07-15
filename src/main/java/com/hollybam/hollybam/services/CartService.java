package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_CartDao;
import com.hollybam.hollybam.dto.CartDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
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
    public List<CartDto> getCartItemsByGuest(int guestCode) {
        return cartDao.getCartItemsByGuest(guestCode);
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
    public int clearCartByGuest(int guestCode) {
        return cartDao.clearCartByGuest(guestCode);
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

    // CartService.java 구현체에 추가할 메서드
    @Override
    @Transactional
    public int addMultipleToCart(List<CartDto> cartItems) {
        int successCount = 0;

        for (CartDto cartItem : cartItems) {
            try {
                // 기존 addToCart 메서드 활용 (중복 체크 로직 포함)
                int result = addToCart(cartItem);
                if (result > 0) {
                    successCount++;
                }

            } catch (Exception e) {
                // 개별 실패는 로그만 남기고 계속 진행
                System.out.println("장바구니 아이템 추가 실패 - productCode: " + cartItem.getProductCode());
                e.printStackTrace();
            }
        }

        System.out.println("다중 장바구니 추가 완료 - 총 " + cartItems.size() + "개 중 " + successCount + "개 성공");
        return successCount;
    }
}
