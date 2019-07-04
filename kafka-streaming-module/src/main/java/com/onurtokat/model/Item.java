package com.onurtokat.model;

public class Item {

    private String productid;
    private int quantity;

    public Item(String productid, int quantity) {
        this.productid = productid;
        this.quantity = quantity;
    }

    public String getProductid() {
        return productid;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Item{" +
                "productid='" + productid + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
