package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_PaymentDao;
import com.hollybam.hollybam.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PaymentService implements IF_PaymentService {

    @Autowired
    private IF_PaymentDao paymentDao;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public PaymentRequestDto calculateOrderFromCart(List<Integer> cartCodes, Integer memCode, Integer guestCode) {
        List<CartDto> cartItems = paymentDao.selectCartItemsWithDetails(cartCodes);

        PaymentRequestDto paymentRequest = new PaymentRequestDto();
        paymentRequest.setCartCodes(cartCodes);
        paymentRequest.setMemberCode(memCode);
        paymentRequest.setGuestCode(guestCode);

        int totalAmount = 0;
        for (CartDto cartItem : cartItems) {
            int itemPrice = cartItem.getPriceDto().getPriceSelling();
            if (cartItem.getOptionCode() != null) {
                itemPrice += cartItem.getProductOptionDto().getOptionPrice();
            }
            totalAmount += itemPrice * cartItem.getQuantity();
        }

        paymentRequest.setTotalAmount(totalAmount);
        paymentRequest.setDiscountAmount(0); // 할인 로직 추가 가능
        paymentRequest.setDeliveryFee(totalAmount >= 50000 ? 0 : 3000); // 5만원 이상 무료배송
        paymentRequest.setFinalAmount(totalAmount - paymentRequest.getDiscountAmount() + paymentRequest.getDeliveryFee());

        return paymentRequest;
    }

    @Override
    public List<CartDto> getCartItemsWithDetails(List<Integer> cartCodes) {
        try {
            return paymentDao.selectCartItemsWithDetails(cartCodes);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public PaymentRequestDto calculateDirectPurchase(int productCode, Integer optionCode, int quantity,
                                                     Integer memCode, Integer guestCode) {
        try {
            PaymentRequestDto paymentRequest = new PaymentRequestDto();
            paymentRequest.setCartCodes(new ArrayList<>()); // 빈 리스트 (바로구매는 cartCode 없음)
            paymentRequest.setMemberCode(memCode);
            paymentRequest.setGuestCode(guestCode);

            // 상품 가격 조회
            PriceDto priceDto = paymentDao.selectProductPrice(productCode);
            int itemPrice = priceDto.getPriceSelling();

            // 옵션 가격 추가 (옵션이 있는 경우)
            if (optionCode != null) {
                ProductOptionDto optionDto = paymentDao.selectProductOption(optionCode);
                itemPrice += optionDto.getOptionPrice();
            }

            // 총 금액 계산
            int totalAmount = itemPrice * quantity;

            paymentRequest.setTotalAmount(totalAmount);
            paymentRequest.setDiscountAmount(0); // 할인 로직 추가 가능
            paymentRequest.setDeliveryFee(totalAmount >= 50000 ? 0 : 3000); // 5만원 이상 무료배송
            paymentRequest.setFinalAmount(totalAmount - paymentRequest.getDiscountAmount() + paymentRequest.getDeliveryFee());

            return paymentRequest;

        } catch (Exception e) {
            e.printStackTrace();
            // 오류 발생 시 기본값 반환
            PaymentRequestDto paymentRequest = new PaymentRequestDto();
            paymentRequest.setCartCodes(new ArrayList<>());
            paymentRequest.setMemberCode(memCode);
            paymentRequest.setGuestCode(guestCode);
            paymentRequest.setTotalAmount(0);
            paymentRequest.setDiscountAmount(0);
            paymentRequest.setDeliveryFee(3000);
            paymentRequest.setFinalAmount(3000);
            return paymentRequest;
        }
    }
}