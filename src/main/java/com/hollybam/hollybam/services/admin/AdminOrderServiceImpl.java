package com.hollybam.hollybam.services.admin;

import com.hollybam.hollybam.dao.admin.IF_AdminOrderDao;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AdminOrderServiceImpl implements IF_AdminOrderService {
    @Autowired
    private IF_AdminOrderDao adminOrderDao;

    @Override
    @Transactional
    public int getTotalOrderCount(){
        return adminOrderDao.getTotalOrderCount();
    }

    @Override
    @Transactional
    public int getPendingOrderCount(){
        return adminOrderDao.getPendingOrderCount();
    }

    @Override
    @Transactional
    public int getPaidOrderCount(){
        return adminOrderDao.getPaidOrderCount();
    }

    @Override
    @Transactional
    public int getOrderPendingOrderCount(){
        return adminOrderDao.getOrderPendingOrderCount();
    }

    @Override
    @Transactional
    public int getShippingOrderCount(){
        return adminOrderDao.getShippingOrderCount();
    }

    @Override
    @Transactional
    public int getDeliveryOrderCount(){
        return adminOrderDao.getDeliveryOrderCount();
    }

    @Override
    @Transactional
    public Map<String, Object> getOrderCounts() {
        return adminOrderDao.getOrderCounts();
    }

    @Override
    @Transactional
    public List<Map<String, Object>> selectOrderSummaryWithStatus() {
        return adminOrderDao.selectOrderSummaryWithStatus();
    }

    @Override
    @Transactional
    public List<Map<String, Object>> getOrderListPending(){
        return adminOrderDao.getOrderListPending();
    }

    @Override
    @Transactional
    public List<Map<String, Object>> getOrderListPaid(){
        return adminOrderDao.getOrderListPaid();
    }

    @Override
    @Transactional
    public List<Map<String, Object>> getOrderListOrderPending(){
        return adminOrderDao.getOrderListOrderPending();
    }

    @Override
    @Transactional
    public List<Map<String, Object>> getOrderListShipping(){
        return adminOrderDao.getOrderListShipping();
    }

    @Override
    @Transactional
    public List<Map<String, Object>> getOrderListDelivered(){
        return adminOrderDao.getOrderListDelivered();
    }

    @Override
    @Transactional
    public Map<String, Object>selectOrderDetail(String orderId) {
        return adminOrderDao.selectOrderDetail(orderId);
    }

    @Override
    @Transactional
    public List<Map<String, Object>> selectOrderItemsByOrderCode(int orderCode) {
        return adminOrderDao.selectOrderItemsByOrderCode(orderCode);
    }

    @Override
    @Transactional
    public Map<String, Object> getDiscountType(int orderCode) {
        return adminOrderDao.getDiscountType(orderCode);
    }

    @Override
    @Transactional
    public int getOrderCodeByOrderId(String orderId){
        return adminOrderDao.getOrderCodeByOrderId(orderId);
    }

    @Override
    @Transactional
    public int getDeliveryStatusCount(int orderCode){
        return adminOrderDao.getDeliveryStatusCount(orderCode);
    }

    @Override
    @Transactional
    public void updateDeliveryStatusNull(List<Integer> orderCodes){
        adminOrderDao.updateDeliveryStatusNull(orderCodes);
    }

    @Override
    @Transactional
    public void updatePayPendingStatus(List<Integer> orderCodes){
        adminOrderDao.updatePayPendingStatus(orderCodes);
    }

    @Override
    @Transactional
    public void updatePaidStatus(List<Integer> orderCodes){
        adminOrderDao.updatePaidStatus(orderCodes);
    }

    @Override
    @Transactional
    public void updateOrderPendingStatus(List<Integer> orderCodes){
        adminOrderDao.updateOrderPendingStatus(orderCodes);
    }

    @Override
    @Transactional
    public void updateShippingStatus(List<Map<String, Object>> orders){
        for(int i=0; i<orders.size();i++){
            orders.get(i).put("deliveryMemo", adminOrderDao.getDeliveryMemo(Integer.parseInt(orders.get(i).get("orderCode").toString())));
        }
        adminOrderDao.insertDeliveryStatus(orders);
        List<Integer> orderCodes = new ArrayList<Integer>();
        for(int i=0; i<orders.size();i++){
            orderCodes.add(Integer.parseInt(orders.get(i).get("orderCode").toString()));
        }
        adminOrderDao.updateShippingStatus(orderCodes);
    }

    @Override
    @Transactional
    public void updateDeliveredStatus(List<Integer> orderCodes){
        adminOrderDao.updateDeliveredStatus(orderCodes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchOrdersWithConditions(Map<String, Object> searchParams) {
        try {
            log.info("검색 조건으로 주문 목록 조회: {}", searchParams);

            // 페이지네이션 처리
            int page = (Integer) searchParams.getOrDefault("page", 1);
            int size = (Integer) searchParams.getOrDefault("size", 20);
            int offset = (page - 1) * size;

            // 검색 파라미터에 offset, limit 추가
            searchParams.put("offset", offset);
            searchParams.put("limit", size);

            // DAO 호출
            List<Map<String, Object>> result = adminOrderDao.searchOrdersWithConditions(searchParams);

            log.info("검색된 주문 개수: {}", result.size());
            return result;

        } catch (Exception e) {
            log.error("검색 조건으로 주문 목록 조회 중 오류 발생", e);
            throw new RuntimeException("주문 검색 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int getSearchOrderCount(Map<String, Object> searchParams) {
        try {
            log.info("검색 조건으로 주문 개수 조회: {}", searchParams);

            int count = adminOrderDao.getSearchOrderCount(searchParams);

            log.info("검색된 총 주문 개수: {}", count);
            return count;

        } catch (Exception e) {
            log.error("검색 조건으로 주문 개수 조회 중 오류 발생", e);
            throw new RuntimeException("주문 개수 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int countOrdersTotal(){
        return adminOrderDao.countOrdersTotal();
    }

    @Override
    @Transactional(readOnly = true)
    public void exportOrdersToExcel(String startDate, String endDate, HttpServletResponse response) throws Exception {
        try {
            log.info("엑셀 내보내기 시작 - 시작일: {}, 종료일: {}", startDate, endDate);

            // 1. 데이터 조회
            List<Map<String, Object>> orderData = adminOrderDao.getOrdersForExcel(startDate, endDate);
            log.info("조회된 주문 데이터 개수: {}", orderData.size());

            // 2. 엑셀 워크북 생성
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("주문 목록");

            // 3. 헤더 스타일 설정
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 4. 데이터 셀 스타일 설정
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // 5. 헤더 생성
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "주문번호", "배송메세지", "할인제외결제금액", "최종결제금액", "상품ID",
                    "상품명", "옵션명", "수량", "수령인", "수령인휴대폰",
                    "우편번호", "주소", "상세주소", "결제여부", "결제수단",
                    "쿠폰ID", "할인코드ID", "배송시작일", "주문일시"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 6. 데이터 행 생성
            int rowNum = 1;
            for (Map<String, Object> order : orderData) {
                Row row = sheet.createRow(rowNum++);

                // 각 컬럼 데이터 설정
                setCellValue(row.createCell(0), order.get("orderNumber"), dataStyle);
                setCellValue(row.createCell(1), order.get("deliveryMessage"), dataStyle);
                setCellValue(row.createCell(2), order.get("totalAmountBeforeDiscount"), dataStyle);
                setCellValue(row.createCell(3), order.get("finalAmount"), dataStyle);
                setCellValue(row.createCell(4), order.get("productId"), dataStyle);
                setCellValue(row.createCell(5), order.get("productName"), dataStyle);
                setCellValue(row.createCell(6), order.get("optionName"), dataStyle);
                setCellValue(row.createCell(7), order.get("quantity"), dataStyle);
                setCellValue(row.createCell(8), order.get("receiverName"), dataStyle);
                setCellValue(row.createCell(9), order.get("receiverPhone"), dataStyle);
                setCellValue(row.createCell(10), order.get("receiverZip"), dataStyle);
                setCellValue(row.createCell(11), order.get("receiverAddr"), dataStyle);
                setCellValue(row.createCell(12), order.get("receiverAddrDetail"), dataStyle);
                setCellValue(row.createCell(13), order.get("paymentStatus"), dataStyle);
                setCellValue(row.createCell(14), order.get("paymentMethod"), dataStyle);
                setCellValue(row.createCell(15), order.get("couponId"), dataStyle);
                setCellValue(row.createCell(16), order.get("discountCodeId"), dataStyle);
                setCellValue(row.createCell(17), order.get("shippingStartDate"), dataStyle);
                setCellValue(row.createCell(18), order.get("orderDate"), dataStyle);
            }

            // 7. 컬럼 너비 자동 조정
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // 최대 너비 제한 (너무 넓어지는 것 방지)
                if (sheet.getColumnWidth(i) > 6000) {
                    sheet.setColumnWidth(i, 6000);
                }
            }

            // 8. HTTP 응답 설정
            String fileName = generateFileName(startDate, endDate);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

            // 9. 파일 출력
            workbook.write(response.getOutputStream());
            workbook.close();

            log.info("엑셀 내보내기 완료 - 파일명: {}", fileName);

        } catch (Exception e) {
            log.error("엑셀 내보내기 중 오류 발생", e);
            throw new RuntimeException("엑셀 파일 생성 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 셀 값 설정 헬퍼 메서드
     */
    private void setCellValue(Cell cell, Object value, CellStyle style) {
        cell.setCellStyle(style);

        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof java.sql.Timestamp) {
            java.sql.Timestamp timestamp = (java.sql.Timestamp) value;
            String formattedDate = timestamp.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            cell.setCellValue(formattedDate);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /**
     * 파일명 생성 헬퍼 메서드
     */
    private String generateFileName(String startDate, String endDate) {
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        if (startDate != null && endDate != null) {
            return String.format("주문목록_%s_%s_%s.xlsx", startDate, endDate, currentDate);
        } else if (startDate != null) {
            return String.format("주문목록_%s이후_%s.xlsx", startDate, currentDate);
        } else if (endDate != null) {
            return String.format("주문목록_%s이전_%s.xlsx", endDate, currentDate);
        } else {
            return String.format("주문목록_전체_%s.xlsx", currentDate);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getOrdererName(@Param("orderCode") int orderCode){
        return  adminOrderDao.getOrdererName(orderCode);
    }

    @Override
    @Transactional(readOnly = true)
    public String getOrdererPhone(@Param("orderCode") int orderCode){
        return  adminOrderDao.getOrdererPhone(orderCode);
    }

    @Override
    @Transactional(readOnly = true)
    public void exportSelectedOrdersToExcel(List<Integer> selectedOrders, HttpServletResponse response) throws Exception {
        try {
            log.info("선택된 주문 엑셀 내보내기 시작 - 선택된 주문 개수: {}", selectedOrders.size());

            // 1. 선택된 주문 데이터 조회
            List<Map<String, Object>> orderData = adminOrderDao.getSelectedOrdersForExcel(selectedOrders);
            log.info("조회된 선택 주문 데이터 개수: {}", orderData.size());

            if (orderData.isEmpty()) {
                throw new RuntimeException("선택된 주문 데이터가 없습니다.");
            }

            // 2. 엑셀 워크북 생성
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("선택된 주문 목록");

            // 3. 헤더 스타일 설정
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 4. 데이터 셀 스타일 설정
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // 5. 헤더 생성 (기존과 동일)
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "주문번호", "배송메세지", "할인제외결제금액", "최종결제금액", "상품ID",
                    "상품명", "옵션명", "수량", "수령인", "수령인휴대폰",
                    "우편번호", "주소", "상세주소", "결제여부", "결제수단",
                    "쿠폰ID", "할인코드ID", "배송시작일", "주문일시"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 6. 데이터 행 생성 (기존 쿼리 컬럼명에 맞춤)
            int rowIndex = 1;
            for (Map<String, Object> order : orderData) {
                Row dataRow = sheet.createRow(rowIndex++);

                // 기존 getOrdersForExcel과 동일한 컬럼명 사용
                setCellValue(dataRow.createCell(0), order.get("orderNumber"), dataStyle);
                setCellValue(dataRow.createCell(1), order.get("deliveryMessage"), dataStyle);
                setCellValue(dataRow.createCell(2), order.get("totalAmountBeforeDiscount"), dataStyle);
                setCellValue(dataRow.createCell(3), order.get("finalAmount"), dataStyle);
                setCellValue(dataRow.createCell(4), order.get("productId"), dataStyle);
                setCellValue(dataRow.createCell(5), order.get("productName"), dataStyle);
                setCellValue(dataRow.createCell(6), order.get("optionName"), dataStyle);
                setCellValue(dataRow.createCell(7), order.get("quantity"), dataStyle);
                setCellValue(dataRow.createCell(8), order.get("receiverName"), dataStyle);
                setCellValue(dataRow.createCell(9), order.get("receiverPhone"), dataStyle);
                setCellValue(dataRow.createCell(10), order.get("receiverZip"), dataStyle);
                setCellValue(dataRow.createCell(11), order.get("receiverAddr"), dataStyle);
                setCellValue(dataRow.createCell(12), order.get("receiverAddrDetail"), dataStyle);
                setCellValue(dataRow.createCell(13), order.get("paymentStatus"), dataStyle);
                setCellValue(dataRow.createCell(14), order.get("paymentMethod"), dataStyle);
                setCellValue(dataRow.createCell(15), order.get("couponId"), dataStyle);
                setCellValue(dataRow.createCell(16), order.get("discountCodeId"), dataStyle);
                setCellValue(dataRow.createCell(17), order.get("shippingStartDate"), dataStyle);
                setCellValue(dataRow.createCell(18), order.get("orderDate"), dataStyle);
            }

            // 7. 컬럼 너비 자동 조정
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // 최대 너비 제한 (너무 넓어지는 것 방지)
                if (sheet.getColumnWidth(i) > 6000) {
                    sheet.setColumnWidth(i, 6000);
                }
            }

            // 8. HTTP 응답 설정 (선택된 주문 전용 파일명)
            String fileName = generateSelectedOrdersFileName(selectedOrders.size());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

            // 9. 파일 출력
            workbook.write(response.getOutputStream());
            workbook.close();

            log.info("선택된 주문 엑셀 내보내기 완료 - 파일명: {}", fileName);

        } catch (Exception e) {
            log.error("선택된 주문 엑셀 내보내기 중 오류 발생", e);
            throw new RuntimeException("선택된 주문 엑셀 파일 생성 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 선택된 주문 파일명 생성 헬퍼 메서드
     */
    private String generateSelectedOrdersFileName(int selectedCount) {
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("선택주문목록_%d개_%s.xlsx", selectedCount, currentDate);
    }

}
