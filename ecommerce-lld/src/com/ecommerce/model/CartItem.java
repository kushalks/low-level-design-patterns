package com.ecommerce.model;

/**
 * A single line in the shopping cart: a product and the desired quantity.
 */
public class CartItem {
    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getLineTotal() {
        return product.getPrice() * quantity;
    }
}
