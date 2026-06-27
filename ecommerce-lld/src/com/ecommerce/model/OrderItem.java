package com.ecommerce.model;

/**
 * A frozen snapshot of a product at the time of ordering.
 *
 * Why snapshot the price? A product's price can change after the order is placed.
 * The order must always reflect what the customer actually paid, so we capture
 * priceAtPurchase instead of reading product.getPrice() later.
 */
public class OrderItem {
    private final String productId;
    private final String productName;
    private final double priceAtPurchase;
    private final int quantity;

    public OrderItem(Product product, int quantity) {
        this.productId = product.getId();
        this.productName = product.getName();
        this.priceAtPurchase = product.getPrice();
        this.quantity = quantity;
    }

    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public double getPriceAtPurchase() { return priceAtPurchase; }
    public int getQuantity() { return quantity; }

    public double getLineTotal() {
        return priceAtPurchase * quantity;
    }
}
