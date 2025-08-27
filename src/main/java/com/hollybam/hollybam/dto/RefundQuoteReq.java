package com.hollybam.hollybam.dto;

import java.util.List;

public class RefundQuoteReq {
    private int orderCode;
    private String actionType;     // "cancel" | "return"
    private String cancelReason;   // 예: "상품불량", "단순변심"
    private List<Item> products;

    public static class Item {
        private int orderItemCode;
        private int selectedQuantity;

        public int getOrderItemCode() { return orderItemCode; }
        public void setOrderItemCode(int orderItemCode) { this.orderItemCode = orderItemCode; }
        public int getSelectedQuantity() { return selectedQuantity; }
        public void setSelectedQuantity(int selectedQuantity) { this.selectedQuantity = selectedQuantity; }
    }

    public int getOrderCode() { return orderCode; }
    public void setOrderCode(int orderCode) { this.orderCode = orderCode; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public List<Item> getProducts() { return products; }
    public void setProducts(List<Item> products) { this.products = products; }
}
