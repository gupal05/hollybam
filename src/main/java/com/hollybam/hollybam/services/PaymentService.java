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
}