package com.diego.rolli;

public class  OrderItem {
    private String productName;
    private int quantity;
    private double pricePerUnit;

    public OrderItem() {
        this.productName = "";
        this.quantity = 0;
        this.pricePerUnit = 0.0;
    }

    public OrderItem(String productName, int quantity, double pricePerUnit) {
        this.productName = productName != null ? productName : "";
        this.quantity = Math.max(0, quantity);
        this.pricePerUnit = Math.max(0.0, pricePerUnit);
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName != null ? productName : "";
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, quantity);
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = Math.max(0.0, pricePerUnit);
    }

    public double getTotalPrice() {
        return quantity * pricePerUnit;
    }
}
