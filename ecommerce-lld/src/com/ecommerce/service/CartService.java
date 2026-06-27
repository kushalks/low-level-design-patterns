package com.ecommerce.service;

import com.ecommerce.model.Cart;
import com.ecommerce.model.Product;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages one cart per user. Validates against live inventory before adding so
 * the customer gets early feedback (final reservation still happens at checkout).
 */
public class CartService {
    private final Map<String, Cart> cartsByUser = new ConcurrentHashMap<>();
    private final InventoryService inventoryService;

    public CartService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public Cart getOrCreateCart(String userId) {
        return cartsByUser.computeIfAbsent(userId, Cart::new);
    }

    public void addToCart(String userId, Product product, int quantity) {
        if (!inventoryService.isAvailable(product.getId(), quantity)) {
            throw new IllegalStateException("Not enough stock for " + product.getName());
        }
        getOrCreateCart(userId).addItem(product, quantity);
    }

    public void removeFromCart(String userId, String productId) {
        getOrCreateCart(userId).removeItem(productId);
    }

    public void clearCart(String userId) {
        getOrCreateCart(userId).clear();
    }
}
