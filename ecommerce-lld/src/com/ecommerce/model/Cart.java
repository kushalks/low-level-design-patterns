package com.ecommerce.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shopping cart belonging to a user. Holds line items keyed by productId so that
 * adding the same product twice just increments the quantity.
 */
public class Cart {
    private final String userId;
    // LinkedHashMap preserves insertion order for a stable display.
    private final Map<String, CartItem> items = new LinkedHashMap<>();

    public Cart(String userId) {
        this.userId = userId;
    }

    public String getUserId() { return userId; }

    public void addItem(Product product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        items.compute(product.getId(), (id, existing) -> {
            if (existing == null) {
                return new CartItem(product, quantity);
            }
            existing.setQuantity(existing.getQuantity() + quantity);
            return existing;
        });
    }

    public void removeItem(String productId) {
        items.remove(productId);
    }

    public void updateQuantity(String productId, int quantity) {
        CartItem item = items.get(productId);
        if (item == null) return;
        if (quantity <= 0) {
            items.remove(productId);
        } else {
            item.setQuantity(quantity);
        }
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(items.values()));
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public double getSubtotal() {
        return items.values().stream().mapToDouble(CartItem::getLineTotal).sum();
    }

    public void clear() {
        items.clear();
    }
}

