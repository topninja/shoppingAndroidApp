package com.entage.nrd.entage.payment;

public class LineItem {

    private String name;
    private String quantity;
    private String unitAmount;
    private String totalAmount;
    private String taxAmount;
    private String discountAmount;
    private Object productCode;
    private String kind;

    public LineItem() {
    }

    public LineItem(String name, String quantity, String unitAmount, String totalAmount, String taxAmount, String discountAmount,
                    Object productCode) {
        this.name = name;
        this.quantity = quantity;
        this.unitAmount = unitAmount;
        this.totalAmount = totalAmount;
        this.taxAmount = taxAmount;
        this.discountAmount = discountAmount;
        this.productCode = productCode;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getUnitAmount() {
        return unitAmount;
    }

    public void setUnitAmount(String unitAmount) {
        this.unitAmount = unitAmount;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(String taxAmount) {
        this.taxAmount = taxAmount;
    }

    public String getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(String discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Object getProductCode() {
        return productCode;
    }

    public void setProductCode(Object productCode) {
        this.productCode = productCode;
    }
}
