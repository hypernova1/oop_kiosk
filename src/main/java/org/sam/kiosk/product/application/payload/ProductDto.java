package org.sam.kiosk.product.application.payload;

import org.sam.kiosk.product.domain.Product;

public record ProductDto(String productNo, String name, int price, int quantity) {
    public ProductDto(Product product) {
        this(product.getProductNo(), product.getName(), product.getPrice(), product.getStock().getAmount());
    }
}
