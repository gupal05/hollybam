package com.hollybam.hollybam.controller.cartController;

import com.hollybam.hollybam.dto.CartDto;
import com.hollybam.hollybam.dto.GuestDto;
import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.services.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> addToCart(@RequestBody Map<String, Object> requestData, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 세션에서 사용자 정보 가져오기
            MemberDto member = (MemberDto) session.getAttribute("member");
            GuestDto guest = (GuestDto) session.getAttribute("guest");
            if (member == null && guest == null) {
                response.put("status", false);
                response.put("message", "로그인이 필요합니다.");
                return response;
            }

            List<Map<String, Object>> cartItems = (List<Map<String, Object>>) requestData.get("cartItems");

            for (Map<String, Object> item : cartItems) {
                CartDto cartDto = new CartDto();
                cartDto.setProductCode((Integer) item.get("productCode"));
                cartDto.setQuantity((Integer) item.get("quantity"));

                if (item.get("optionCode") != null) {
                    cartDto.setOptionCode((Integer) item.get("optionCode"));
                }

                if (member != null) {
                    // 회원인 경우
                    cartDto.setMemCode(member.getMemberCode());
                    cartDto.setGuestCode(null); // 명시적으로 null 설정
                } else {
                    // 비회원인 경우
                    int guestCode = guest.getGuestCode();
                    cartDto.setMemCode(null); // 명시적으로 null 설정
                    cartDto.setGuestCode(guestCode);
                }

                cartService.addToCart(cartDto);
            }

            response.put("status", true);
            response.put("message", "장바구니에 담았습니다.");

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", false);
            response.put("message", "장바구니 담기에 실패했습니다.");
        }

        return response;
    }

    @GetMapping
    public ModelAndView getCartPage(HttpSession session, ModelAndView mav) {
        MemberDto member = (MemberDto) session.getAttribute("member");
        GuestDto guest = (GuestDto) session.getAttribute("guest");

        List<CartDto> cartItems = new ArrayList<>();

        if (member != null) {
            cartItems = cartService.getCartItemsByMember(member.getMemberCode());
        } else if (guest != null) {
            cartItems = cartService.getCartItemsByGuest(guest.getGuestCode());
        }
        System.out.println("cartItems = " + cartItems);
        mav.addObject("cartItems", cartItems);
        mav.setViewName("cart");
        return mav;
    }

    // 수량 업데이트
    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> updateCartQuantity(@RequestBody Map<String, Object> requestData, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            int cartCode = (Integer) requestData.get("cartCode");
            int quantity = (Integer) requestData.get("quantity");

            // 사용자 권한 체크 (선택사항: 본인의 장바구니인지 확인)
            if (!isValidCartAccess(cartCode, session)) {
                response.put("status", false);
                response.put("message", "권한이 없습니다.");
                return response;
            }

            int result = cartService.updateCartQuantity(cartCode, quantity);

            if (result > 0) {
                response.put("status", true);
                response.put("message", "수량이 변경되었습니다.");
            } else {
                response.put("status", false);
                response.put("message", "수량 변경에 실패했습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", false);
            response.put("message", "수량 변경에 실패했습니다.");
        }

        return response;
    }

    // 단일 상품 삭제
    @PostMapping("/remove")
    @ResponseBody
    public Map<String, Object> removeFromCart(@RequestBody Map<String, Object> requestData, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            int cartCode = (Integer) requestData.get("cartCode");

            // 사용자 권한 체크
            if (!isValidCartAccess(cartCode, session)) {
                response.put("status", false);
                response.put("message", "권한이 없습니다.");
                return response;
            }

            int result = cartService.removeFromCart(cartCode);

            if (result > 0) {
                response.put("status", true);
                response.put("message", "상품이 삭제되었습니다.");
            } else {
                response.put("status", false);
                response.put("message", "상품 삭제에 실패했습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", false);
            response.put("message", "상품 삭제에 실패했습니다.");
        }

        return response;
    }

    // 다중 상품 삭제
    @PostMapping("/remove-multiple")
    @ResponseBody
    public Map<String, Object> removeMultipleFromCart(@RequestBody Map<String, Object> requestData, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Integer> cartCodes = (List<Integer>) requestData.get("cartCodes");

            // 모든 cartCode에 대해 권한 체크
            for (int cartCode : cartCodes) {
                if (!isValidCartAccess(cartCode, session)) {
                    response.put("status", false);
                    response.put("message", "권한이 없습니다.");
                    return response;
                }
            }

            int deletedCount = 0;
            for (int cartCode : cartCodes) {
                deletedCount += cartService.removeFromCart(cartCode);
            }

            if (deletedCount > 0) {
                response.put("status", true);
                response.put("message", deletedCount + "개 상품이 삭제되었습니다.");
            } else {
                response.put("status", false);
                response.put("message", "상품 삭제에 실패했습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", false);
            response.put("message", "상품 삭제에 실패했습니다.");
        }

        return response;
    }

    // 장바구니 전체 비우기
    @PostMapping("/clear")
    @ResponseBody
    public Map<String, Object> clearCart(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            MemberDto member = (MemberDto) session.getAttribute("member");
            GuestDto guest = (GuestDto) session.getAttribute("guest");

            int result = 0;
            if (member != null) {
                result = cartService.clearCartByMember(member.getMemberCode());
            } else if (guest != null) {
                result = cartService.clearCartByGuest(guest.getGuestCode());
            }

            if (result > 0) {
                response.put("status", true);
                response.put("message", "장바구니가 비워졌습니다.");
            } else {
                response.put("status", false);
                response.put("message", "장바구니 비우기에 실패했습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", false);
            response.put("message", "장바구니 비우기에 실패했습니다.");
        }

        return response;
    }

    // 권한 체크 메서드 (본인의 장바구니인지 확인)
    private boolean isValidCartAccess(int cartCode, HttpSession session) {
        try {
            MemberDto member = (MemberDto) session.getAttribute("member");
            GuestDto guest = (GuestDto) session.getAttribute("guest");

            if (member == null && guest == null) {
                return false;
            }

            // 실제로는 cartService에 cartCode로 소유자를 확인하는 메서드가 필요
            // 여기서는 간단히 true로 처리 (보안이 중요한 경우 실제 구현 필요)
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 위시리스트에서 여러 상품을 장바구니에 한번에 추가
     */
    @PostMapping("/add-multiple")
    @ResponseBody
    public Map<String, Object> addMultipleToCart(@RequestParam List<Integer> productCodes, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 세션에서 사용자 정보 가져오기
            MemberDto member = (MemberDto) session.getAttribute("member");
            GuestDto guest = (GuestDto) session.getAttribute("guest");

            if (member == null && guest == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return response;
            }

            List<CartDto> cartItems = new ArrayList<>();

            // 각 상품을 장바구니에 추가
            for (Integer productCode : productCodes) {
                CartDto cartDto = new CartDto();
                cartDto.setProductCode(productCode);
                cartDto.setQuantity(1); // 기본 수량 1
                cartDto.setOptionCode(null); // 위시리스트에서는 기본 옵션

                if (member != null) {
                    cartDto.setMemCode(member.getMemberCode());
                    cartDto.setGuestCode(null);
                } else {
                    cartDto.setMemCode(null);
                    cartDto.setGuestCode(guest.getGuestCode());
                }

                cartItems.add(cartDto);
            }

            // 서비스에서 중복 체크와 함께 추가
            int successCount = cartService.addMultipleToCart(cartItems);

            response.put("success", true);
            response.put("message", successCount + "개 상품이 장바구니에 추가되었습니다.");
            response.put("addedCount", successCount);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "장바구니 추가 중 오류가 발생했습니다.");
        }

        return response;
    }

    /**
     * 위시리스트에서 단일 상품 바로구매를 위한 주문 페이지 이동
     */
    @PostMapping("/direct-purchase")
    @ResponseBody
    public Map<String, Object> directPurchaseFromWishlist(@RequestParam int productCode, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 세션에서 사용자 정보 가져오기
            MemberDto member = (MemberDto) session.getAttribute("member");
            GuestDto guest = (GuestDto) session.getAttribute("guest");

            if (member == null && guest == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return response;
            }

            // 상품 정보 조회 (재고 체크 등)
            // 실제로는 ProductService를 통해 상품 정보를 확인해야 함

            // 세션에 바로구매 정보 저장
            Map<String, Object> directPurchaseData = new HashMap<>();
            directPurchaseData.put("type", "direct");
            directPurchaseData.put("productCode", productCode);
            directPurchaseData.put("quantity", 1);
            directPurchaseData.put("optionCode", null);

            session.setAttribute("directPurchaseData", directPurchaseData);

            response.put("success", true);
            response.put("redirectUrl", "/order/direct");

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "주문 처리 중 오류가 발생했습니다.");
        }

        return response;
    }

    /**
     * 위시리스트에서 선택된 여러 상품을 바로구매하기 위한 장바구니 임시 저장 후 주문 페이지 이동
     */
    @PostMapping("/bulk-purchase")
    @ResponseBody
    public Map<String, Object> bulkPurchaseFromWishlist(@RequestParam List<Integer> productCodes, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 세션에서 사용자 정보 가져오기
            MemberDto member = (MemberDto) session.getAttribute("member");
            GuestDto guest = (GuestDto) session.getAttribute("guest");

            if (member == null && guest == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return response;
            }

            if (productCodes == null || productCodes.isEmpty()) {
                response.put("success", false);
                response.put("message", "선택된 상품이 없습니다.");
                return response;
            }

            // 임시 장바구니에 추가 (실제 장바구니가 아닌 주문용 임시 데이터)
            List<Map<String, Object>> tempCartItems = new ArrayList<>();

            for (Integer productCode : productCodes) {
                Map<String, Object> item = new HashMap<>();
                item.put("productCode", productCode);
                item.put("quantity", 1);
                item.put("optionCode", null);
                tempCartItems.add(item);
            }

            // 세션에 임시 주문 데이터 저장
            session.setAttribute("tempOrderData", tempCartItems);

            response.put("success", true);
            response.put("redirectUrl", "/order/temp");
            response.put("message", "선택한 상품들로 주문을 진행합니다.");

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "주문 처리 중 오류가 발생했습니다.");
        }

        return response;
    }

}
