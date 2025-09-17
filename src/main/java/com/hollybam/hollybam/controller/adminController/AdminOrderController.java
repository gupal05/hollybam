package com.hollybam.hollybam.controller.adminController;

import com.hollybam.hollybam.services.admin.IF_AdminOrderService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RequestMapping("/admin/order")
@Controller
public class AdminOrderController {
    @Autowired
    private IF_AdminOrderService adminOrderService;

    @GetMapping
    public String adminOrderPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "ALL") String status,
            // 🆕 검색 파라미터 추가
            @RequestParam(required = false) String orderIdFilter,
            @RequestParam(required = false) String ordererNameFilter,
            @RequestParam(required = false) String productNameFilter,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String paymentMethodFilter,
            Model model) {

        Map<String, Object> orderCount = adminOrderService.getOrderCounts();
        int totalCount = 0;
        List<Map<String, Object>> orderList = new ArrayList<>();

        // 🔍 검색 조건 확인 - 하나라도 있으면 검색 모드
        boolean hasSearchCondition = (orderIdFilter != null && !orderIdFilter.trim().isEmpty()) ||
                (ordererNameFilter != null && !ordererNameFilter.trim().isEmpty()) ||
                (productNameFilter != null && !productNameFilter.trim().isEmpty()) ||
                (startDate != null && !startDate.trim().isEmpty()) ||
                (endDate != null && !endDate.trim().isEmpty()) ||
                (paymentMethodFilter != null && !paymentMethodFilter.trim().isEmpty());

        if (hasSearchCondition) {
            // 🆕 검색 모드 - 새로운 검색 메서드 사용
            Map<String, Object> searchParams = new HashMap<>();
            searchParams.put("status", status);
            searchParams.put("orderIdFilter", orderIdFilter);
            searchParams.put("ordererNameFilter", ordererNameFilter);
            searchParams.put("productNameFilter", productNameFilter);
            searchParams.put("startDate", startDate);
            searchParams.put("endDate", endDate);
            searchParams.put("paymentMethodFilter", paymentMethodFilter);
            searchParams.put("page", page);
            searchParams.put("size", size);

            // 검색 결과 조회
            orderList = adminOrderService.searchOrdersWithConditions(searchParams);
            totalCount = adminOrderService.getSearchOrderCount(searchParams);

        } else {
            // 💪 기존 로직 그대로 유지 (기존 기능 보존)
            if(status.equals("ALL")){
                orderList = adminOrderService.selectOrderSummaryWithStatus();
                totalCount = adminOrderService.getTotalOrderCount();
            } else if(status.equals("payPending")){
                orderList = adminOrderService.getOrderListPending();
                totalCount = adminOrderService.getPendingOrderCount();
            } else if(status.equals("paid")){
                orderList = adminOrderService.getOrderListPaid();
                totalCount = adminOrderService.getPaidOrderCount();
            } else if(status.equals("orderPending")){
                orderList = adminOrderService.getOrderListOrderPending();
                totalCount = adminOrderService.getOrderPendingOrderCount();
            } else if(status.equals("shipping")){
                orderList = adminOrderService.getOrderListShipping();
                totalCount = adminOrderService.getShippingOrderCount();
            } else if(status.equals("delivered")){
                orderList = adminOrderService.getOrderListDelivered();
                totalCount = adminOrderService.getDeliveryOrderCount();
            }
        }

        // 🔄 기존 데이터 후처리 로직 그대로 유지
        for(int i=0; i<orderList.size();i++){
            String addr = (String)orderList.get(i).get("receiverAddr");
            String detail =  (String)orderList.get(i).get("receiverAddrDetail");
            orderList.get(i).remove("receiverAddr");
            orderList.get(i).put("receiverAddr", addr+" "+detail);
            long productTypeCount = (long)orderList.get(i).get("productTypeCount");
            if(productTypeCount > 1){
                String productName = (String)orderList.get(i).get("productName");
                orderList.get(i).remove("productName");
                orderList.get(i).put("productName", productName+" 외 "+(productTypeCount-1)+"개");
            }
        }

        // 🔄 기존 페이지네이션 로직 그대로 유지
        int startIndex = (page - 1) * size + 1;
        int endIndex = Math.min(page * size, totalCount);

        DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        orderList.sort(Comparator.comparing((Map<String,Object> m) -> {
            Object v = m.get("createAt");
            return (v instanceof LocalDateTime ldt) ?
                    ldt : LocalDateTime.parse(String.valueOf(v), ISO);
        }).reversed()); // 최신순

        // 🔄 기존 모델 추가 로직 그대로 유지 + 검색 조건 추가
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("startIndex", startIndex);
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("orderCount", orderCount);
        model.addAttribute("currentStatus", status);
        for(int i=0; i<orderList.size();i++){
            int orderCode = (int)orderList.get(i).get("orderCode");
            String ordererName = adminOrderService.getOrdererName(orderCode);
            String ordererPhone = adminOrderService.getOrdererPhone(orderCode);
            orderList.get(i).put("ordererName", ordererName);
            orderList.get(i).put("ordererPhone", ordererPhone);
        }
        model.addAttribute("orderList", orderList);

        model.addAttribute("totalPages", (int) Math.ceil((double) totalCount / size));

        model.addAttribute("toc", adminOrderService.countOrdersTotal());


        // 🆕 개별 카운트 값들을 모델에 추가 (HTML에서 쉽게 접근하기 위함)
        model.addAttribute("totalOrderCount", orderCount.get("totalCount"));
        model.addAttribute("pendingOrderCount", orderCount.get("pendingCount"));
        model.addAttribute("paidOrderCount", orderCount.get("paidCount"));
        model.addAttribute("orderPendingOrderCount", orderCount.get("orderPendingCount"));
        model.addAttribute("shippingOrderCount", orderCount.get("shippingCount"));
        model.addAttribute("deliveredOrderCount", orderCount.get("deliveredCount"));

        // 🆕 검색 조건들을 모델에 추가 (프론트에서 유지하기 위함)
        model.addAttribute("orderIdFilter", orderIdFilter);
        model.addAttribute("ordererNameFilter", ordererNameFilter);
        model.addAttribute("productNameFilter", productNameFilter);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("paymentMethodFilter", paymentMethodFilter);

        return "admin/order/orderManagement";
    }

    @GetMapping("/{orderId}")
    @ResponseBody
    public ResponseEntity<?> getOrder(@PathVariable String orderId, Model model) {
        Map<String, Object> orderDetailData = adminOrderService.selectOrderDetail(orderId);
        int orderCode = (int)orderDetailData.get("orderCode");
        List<Map<String, Object>> orderDetailProductInfo = adminOrderService.selectOrderItemsByOrderCode(orderCode);
        model.addAttribute("orderDetailData",  orderDetailData);
        model.addAttribute("discountInfo", adminOrderService.getDiscountType(orderCode));
        System.out.println(adminOrderService.getDiscountType(orderCode));
        model.addAttribute("orderDetailProductInfo", orderDetailProductInfo);
        return ResponseEntity.ok().body(model);
    }

    @PostMapping("/status/{orderId}")
    @ResponseBody
    public ResponseEntity<?> updateOrderStatus(@PathVariable String orderId, @RequestParam String status) {
        System.out.println(orderId);
        int orderCode = adminOrderService.getOrderCodeByOrderId(orderId);
        System.out.println(status);
        List<Integer> orderCodes = new ArrayList<>();
        orderCodes.add(orderCode);
        if(status.equals("PAYPENDING")) {
            adminOrderService.updatePayPendingStatus(orderCodes);
            if(adminOrderService.getDeliveryStatusCount(orderCode) > 0){
                adminOrderService.updateDeliveryStatusNull(orderCodes);
            }
            System.out.println("결제대기");
        } else if(status.equals("PAID")) {
            adminOrderService.updatePaidStatus(orderCodes);
            if(adminOrderService.getDeliveryStatusCount(orderCode) > 0){
                adminOrderService.updateDeliveryStatusNull(orderCodes);
            }
            System.out.println("결제완료");
        } else if(status.equals("ORDERPENDING")){
            adminOrderService.updateOrderPendingStatus(orderCodes);
            if(adminOrderService.getDeliveryStatusCount(orderCode) > 0){
                adminOrderService.updateDeliveryStatusNull(orderCodes);
            }
            System.out.println("주문접수");
        } else if(status.equals("DELIVERED")){
            adminOrderService.updateDeliveredStatus(orderCodes);
            System.out.println("배송완료");
        }
        return ResponseEntity.ok(true);
    }

    @PostMapping("/status/batch")
    @ResponseBody
    public ResponseEntity<?> updateStatusBatch(@RequestParam String status, @RequestParam List<Integer> orderCodes) {
        System.out.println(orderCodes);
        if(status.equals("PAYPENDING")) {
            adminOrderService.updatePayPendingStatus(orderCodes);
            for (int orderCode : orderCodes) {
                List<Integer> code = new ArrayList<>();
                code.add(orderCode);
                if (adminOrderService.getDeliveryStatusCount(orderCode) > 0) {
                    adminOrderService.updateDeliveryStatusNull(code);
                }
            }
            System.out.println("결제대기");
        } else if(status.equals("PAID")) {
            adminOrderService.updatePaidStatus(orderCodes);
            for (int orderCode : orderCodes) {
                List<Integer> code = new ArrayList<>();
                code.add(orderCode);
                if (adminOrderService.getDeliveryStatusCount(orderCode) > 0) {
                    adminOrderService.updateDeliveryStatusNull(code);
                }
            }
            System.out.println("결제완료");
        } else if(status.equals("ORDERPENDING")){
            adminOrderService.updateOrderPendingStatus(orderCodes);
            for (int orderCode : orderCodes) {
                List<Integer> code = new ArrayList<>();
                code.add(orderCode);
                if (adminOrderService.getDeliveryStatusCount(orderCode) > 0) {
                    adminOrderService.updateDeliveryStatusNull(code);
                }
            }
            System.out.println("주문접수");
        } else if(status.equals("DELIVERED")){
            adminOrderService.updateDeliveredStatus(orderCodes);
            System.out.println("배송완료");
        }
        return ResponseEntity.ok(true);
    }

    @PostMapping(value = "/status/batch-tracking", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> updateStatusBatchTracking(@RequestBody List<Map<String, Object>> orders) {
        System.out.println(orders);
        adminOrderService.updateShippingStatus(orders);
        System.out.println("배송중");
        return ResponseEntity.ok(true);
    }

    /**
     * 주문 데이터 엑셀 내보내기
     * @param startDate 시작 날짜 (yyyy-MM-dd 형식, 선택사항)
     * @param endDate 종료 날짜 (yyyy-MM-dd 형식, 선택사항)
     * @param response HTTP 응답 객체
     */
    @GetMapping("/export")
    public void exportOrdersToExcel(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response) {

        try {
            log.info("주문 엑셀 내보내기 요청 - 시작일: {}, 종료일: {}", startDate, endDate);

            // 날짜 형식 유효성 검사 (간단한 체크)
            if (startDate != null && !startDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                throw new IllegalArgumentException("시작 날짜 형식이 올바르지 않습니다. (yyyy-MM-dd)");
            }

            if (endDate != null && !endDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                throw new IllegalArgumentException("종료 날짜 형식이 올바르지 않습니다. (yyyy-MM-dd)");
            }

            // 날짜 범위 유효성 검사
            if (startDate != null && endDate != null && startDate.compareTo(endDate) > 0) {
                throw new IllegalArgumentException("시작 날짜가 종료 날짜보다 늦을 수 없습니다.");
            }

            // 엑셀 내보내기 실행
            adminOrderService.exportOrdersToExcel(startDate, endDate, response);

            log.info("주문 엑셀 내보내기 완료");

        } catch (IllegalArgumentException e) {
            log.warn("엑셀 내보내기 파라미터 오류: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                response.getWriter().write("파라미터 오류: " + e.getMessage());
            } catch (Exception ex) {
                log.error("에러 응답 작성 실패", ex);
            }
        } catch (Exception e) {
            log.error("주문 엑셀 내보내기 중 오류 발생", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.getWriter().write("엑셀 파일 생성 중 오류가 발생했습니다.");
            } catch (Exception ex) {
                log.error("에러 응답 작성 실패", ex);
            }
        }
    }
}
