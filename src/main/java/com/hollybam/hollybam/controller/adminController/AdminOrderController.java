package com.hollybam.hollybam.controller.adminController;

import com.hollybam.hollybam.services.admin.IF_AdminOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
            Model model) {

        Map<String, Object> orderCount = adminOrderService.getOrderCounts();
        int totalCount = Integer.parseInt(orderCount.get("total").toString());

        List<Map<String, Object>> orderList = adminOrderService.selectOrderSummaryWithStatus();
        for(int i=0; i<orderList.size();i++){
            String addr = (String)orderList.get(i).get("receiverAddr");
            String detail =  (String)orderList.get(i).get("receiverAddrDetail");
            orderList.get(i).remove("receiverAddr");
            orderList.get(i).put("receiverAddr", addr+detail);
            long productTypeCount = (long)orderList.get(i).get("productTypeCount");
            if(productTypeCount > 1){
                String productName = (String)orderList.get(i).get("productName");
                orderList.get(i).remove("productName");
                orderList.get(i).put("productName", productName+" 외 "+(productTypeCount-1)+"개");
            }
        }

        System.out.println(orderList);

        // 페이지네이션 계산
        int startIndex = (page - 1) * size + 1;
        int endIndex = Math.min(page * size, totalCount);

        // 모델에 추가
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("startIndex", startIndex);
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("orderCount", orderCount);
        model.addAttribute("currentStatus", status);
        model.addAttribute("orderList", orderList);
        model.addAttribute("totalPages", (int) Math.ceil((double) totalCount / size));
        return "admin/order/orderManagement";
    }

    @GetMapping("/{orderId}")
    @ResponseBody
    public ResponseEntity<?> getOrder(@PathVariable String orderId, Model model) {
        Map<String, Object> orderDetailData = adminOrderService.selectOrderDetail(orderId);
        int orderCode = (int)orderDetailData.get("orderCode");
        List<Map<String, Object>> orderDetailProductInfo = adminOrderService.selectOrderItemsByOrderCode(orderCode);
        model.addAttribute("orderDetailData",  orderDetailData);
        model.addAttribute("orderDetailProductInfo", orderDetailProductInfo);
        return ResponseEntity.ok().body(model);
    }
}
