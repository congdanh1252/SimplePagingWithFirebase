package com.example.simplepaging;

public class Product {
    String ProductName;
    String ProductId;

    public Product() {}

    public Product(String productName, String productId) {
        ProductName = productName;
        ProductId = productId;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public String getProductId() {
        return ProductId;
    }

    public void setProductId(String productId) {
        ProductId = productId;
    }

}
