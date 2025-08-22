package com.hollybam.hollybam.services.admin;

import com.hollybam.hollybam.controller.adminController.AdminProductController.AdminProductSearchDto;
import com.hollybam.hollybam.controller.adminController.AdminProductController.AdminProductListResultDto;
import com.hollybam.hollybam.dao.admin.IF_AdminProductDao;
import com.hollybam.hollybam.dto.CategoryDto;
import com.hollybam.hollybam.dto.PriceDto;
import com.hollybam.hollybam.dto.ProductDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminProductServiceImpl implements IF_AdminProductService {

    private final IF_AdminProductDao adminProductDao;

    @Override
    @Transactional(readOnly = true)
    public AdminProductListResultDto adminGetProductList(AdminProductSearchDto searchDto) throws Exception {
        try {
            // 페이징을 위한 offset 계산
            int offset = (searchDto.getPage() - 1) * searchDto.getSize();

            // 상품 목록 조회
            List<ProductDto> products = adminProductDao.adminSelectProductList(searchDto, offset);

            // 총 개수 조회
            long totalCount = adminProductDao.adminSelectProductCount(searchDto);

            log.info("관리자 상품 목록 조회 완료 - 조회 개수: {}, 총 개수: {}", products.size(), totalCount);

            return new AdminProductListResultDto(products, totalCount);

        } catch (Exception e) {
            log.error("관리자 상품 목록 조회 중 오류 발생", e);
            throw new Exception("상품 목록 조회에 실패했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> adminGetAllCategories() throws Exception {
        try {
            List<CategoryDto> categories = adminProductDao.adminSelectAllCategories();
            log.info("관리자 카테고리 목록 조회 완료 - 조회 개수: {}", categories.size());
            return categories;

        } catch (Exception e) {
            log.error("관리자 카테고리 목록 조회 중 오류 발생", e);
            throw new Exception("카테고리 목록 조회에 실패했습니다.", e);
        }
    }

    @Override
    public void adminToggleProductStatus(int productCode) throws Exception {
        try {
            // 현재 상태 조회
            ProductDto product = adminProductDao.adminSelectProductDetail(productCode);
            if (product == null) {
                throw new Exception("상품을 찾을 수 없습니다.");
            }

            // 상태 토글
            int newStatus = product.getProductActive() == 1 ? 0 : 1;
            adminProductDao.adminUpdateProductStatus(productCode, newStatus);

            log.info("상품 상태 변경 완료 - 상품코드: {}, 변경된 상태: {}", productCode, newStatus);

        } catch (Exception e) {
            log.error("상품 상태 변경 중 오류 발생 - 상품코드: {}", productCode, e);
            throw new Exception("상품 상태 변경에 실패했습니다.", e);
        }
    }

    @Override
    public void adminDeleteProduct(int productCode) throws Exception {
        try {
            // 상품 존재 여부 확인
            ProductDto product = adminProductDao.adminSelectProductDetail(productCode);
            if (product == null) {
                throw new Exception("상품을 찾을 수 없습니다.");
            }

            // 소프트 삭제 (상태를 -1로 변경)
            adminProductDao.adminUpdateProductStatus(productCode, -1);

            log.info("상품 삭제 완료 - 상품코드: {}", productCode);

        } catch (Exception e) {
            log.error("상품 삭제 중 오류 발생 - 상품코드: {}", productCode, e);
            throw new Exception("상품 삭제에 실패했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto adminGetProductDetail(int productCode) throws Exception {
        try {
            ProductDto product = adminProductDao.adminSelectProductDetail(productCode);
            if (product == null) {
                throw new Exception("상품을 찾을 수 없습니다.");
            }

            log.info("관리자 상품 상세 조회 완료 - 상품코드: {}", productCode);
            return product;

        } catch (Exception e) {
            log.error("관리자 상품 상세 조회 중 오류 발생 - 상품코드: {}", productCode, e);
            throw new Exception("상품 상세 조회에 실패했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long adminGetProductCount(AdminProductSearchDto searchDto) throws Exception {
        try {
            long count = adminProductDao.adminSelectProductCount(searchDto);
            log.info("관리자 상품 개수 조회 완료 - 총 개수: {}", count);
            return count;

        } catch (Exception e) {
            log.error("관리자 상품 개수 조회 중 오류 발생", e);
            throw new Exception("상품 개수 조회에 실패했습니다.", e);
        }
    }

    @Override
    public void adminCreateProductExcel(List<ProductDto> products, HttpServletResponse response) throws Exception {
        try {
            // 워크북 생성
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("상품 목록");

            // 헤더 스타일 생성
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // 데이터 스타일 생성
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setWrapText(true);

            // 가격 스타일 생성
            CellStyle priceStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            priceStyle.setDataFormat(format.getFormat("#,##0"));

            // 헤더 생성
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "상품코드", "상품명", "상품설명", "정가", "판매가", "재고", "판매량", "상태", "등록일", "수정일"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 데이터 행 생성
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            int rowNum = 1;

            for (ProductDto product : products) {
                Row row = sheet.createRow(rowNum++);

                // 상품코드
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(product.getProductId());
                cell0.setCellStyle(dataStyle);

                // 상품명
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(product.getProductName());
                cell1.setCellStyle(dataStyle);

                // 상품설명
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(product.getProductDescription() != null ? product.getProductDescription() : "");
                cell2.setCellStyle(dataStyle);

                // 가격 정보
                if (product.getPriceDtoList() != null && !product.getPriceDtoList().isEmpty()) {
                    PriceDto price = product.getPriceDtoList().get(0);

                    // 정가
                    Cell cell3 = row.createCell(3);
                    cell3.setCellValue(price.getPriceOriginal());
                    cell3.setCellStyle(priceStyle);

                    // 판매가
                    Cell cell4 = row.createCell(4);
                    cell4.setCellValue(price.getPriceSelling());
                    cell4.setCellStyle(priceStyle);
                } else {
                    row.createCell(3).setCellValue("미설정");
                    row.createCell(4).setCellValue("미설정");
                }

                // 재고
                Cell cell5 = row.createCell(5);
                cell5.setCellValue(product.getProductQuantity());
                cell5.setCellStyle(dataStyle);

                // 판매량
                Cell cell6 = row.createCell(6);
                cell6.setCellValue(product.getProductOrderCount());
                cell6.setCellStyle(dataStyle);

                // 상태
                Cell cell7 = row.createCell(7);
                cell7.setCellValue(product.getProductActive() == 1 ? "판매중" : "판매중지");
                cell7.setCellStyle(dataStyle);

                // 등록일
                Cell cell8 = row.createCell(8);
                cell8.setCellValue(product.getCreateAt().format(formatter));
                cell8.setCellStyle(dataStyle);

                // 수정일
                Cell cell9 = row.createCell(9);
                cell9.setCellValue(product.getUpdateAt().format(formatter));
                cell9.setCellStyle(dataStyle);
            }

            // 컬럼 너비 자동 조정
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 응답 헤더 설정
            String fileName = "상품목록_" + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + java.net.URLEncoder.encode(fileName, "UTF-8") + "\"");

            // 파일 출력
            workbook.write(response.getOutputStream());
            workbook.close();

            log.info("관리자 상품 목록 Excel 파일 생성 완료 - 파일명: {}, 상품 수: {}", fileName, products.size());

        } catch (IOException e) {
            log.error("Excel 파일 생성 중 IO 오류 발생", e);
            throw new Exception("Excel 파일 생성에 실패했습니다.", e);
        } catch (Exception e) {
            log.error("Excel 파일 생성 중 오류 발생", e);
            throw new Exception("Excel 파일 생성에 실패했습니다.", e);
        }
    }

    @Override
    public void adminBulkUpdateProductStatus(List<Integer> productCodes, int status) throws Exception {
        try {
            if (productCodes == null || productCodes.isEmpty()) {
                throw new Exception("변경할 상품이 없습니다.");
            }

            // 유효한 상태값 검증
            if (status < -1 || status > 1) {
                throw new Exception("유효하지 않은 상태값입니다.");
            }

            // 상품 존재 여부 확인
            for (Integer productCode : productCodes) {
                ProductDto product = adminProductDao.adminSelectProductDetail(productCode);
                if (product == null) {
                    throw new Exception("존재하지 않는 상품이 포함되어 있습니다. (상품코드: " + productCode + ")");
                }
            }

            // 일괄 상태 변경
            adminProductDao.adminBulkUpdateProductStatus(productCodes, status);

            log.info("관리자 상품 일괄 상태 변경 완료 - 변경된 상품 수: {}, 변경된 상태: {}", productCodes.size(), status);

        } catch (Exception e) {
            log.error("상품 일괄 상태 변경 중 오류 발생", e);
            throw new Exception("상품 상태 일괄 변경에 실패했습니다.", e);
        }
    }

    @Override
    @Transactional
    public Map<String, Object> getProductInfo(int productCode) {
        return adminProductDao.getProductInfo(productCode);
    }

    @Override
    @Transactional
    public List<Map<String, Object>> getEditProductThumbnail(@Param("productCode") int productCode){
        return adminProductDao.getEditProductThumbnail(productCode);
    }

    @Override
    @Transactional
    public List<Map<String, Object>> getEditProductDetail(@Param("productCode") int productCode){
        return adminProductDao.getEditProductDetail(productCode);
    }

    @Override
    @Transactional
    public List<Map<String, Object>> getProductOption(@Param("productCode") int productCode){
        return adminProductDao.getProductOption(productCode);
    }

    @Override
    @Transactional
    public String getOptionName(@Param("productCode") int productCode){
        return adminProductDao.getOptionName(productCode);
    }
}