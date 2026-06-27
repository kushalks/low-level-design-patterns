package com.ecommerce.service;

import com.ecommerce.discount.DiscountStrategy;
import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.User;
import com.ecommerce.notification.OrderObserver;
import com.ecommerce.payment.PaymentStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Orchestrates the checkout workflow. This is the "use case" layer that
 * coordinates inventory, pricing, payment and notifications.
 *
 * Dependencies are injected via the constructor (Dependency Inversion) so each
 * collaborator can be swapped or mocked in tests.
 */
public class OrderService {
    private final InventoryService inventoryService;
    private final CartService cartService;
    private final List<OrderObserver> defaultObservers;
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    public OrderService(InventoryService inventoryService,
                        CartService cartService,
                        List<OrderObserver> defaultObservers) {
        this.inventoryService = inventoryService;
        this.cartService = cartService;
        this.defaultObservers = defaultObservers;
    }

    /**
     * Place an order from the user's cart.
     *
     * Flow:
     *   1. Validate cart is non-empty.
     *   2. Atomically reserve inventory for every line (rollback on partial failure).
     *   3. Compute the payable amount via the chosen DiscountStrategy.
     *   4. Charge via the chosen PaymentStrategy; release stock if it fails.
     *   5. Build the Order, wire up observers, move it to PAID.
     */
    public Order placeOrder(User user,
                            DiscountStrategy discount,
                            PaymentStrategy payment) {
        Cart cart = cartService.getOrCreateCart(user.getId());
        if (cart.isEmpty()) {
            throw new IllegalStateException("Cannot place an order with an empty cart");
        }

        // 2. Reserve inventory atomically; keep track for rollback.
        List<CartItem> reserved = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            boolean ok = inventoryService.reserve(item.getProduct().getId(), item.getQuantity());
            if (!ok) {
                rollback(reserved);
                throw new IllegalStateException("Out of stock: " + item.getProduct().getName());
            }
            reserved.add(item);
        }

        // 3. Pricing.
        double subtotal = cart.getSubtotal();
        double payable = discount.apply(subtotal);
        System.out.println("Subtotal: $" + subtotal + " | " + discount.describe()
                + " | Payable: $" + payable);

        // 4. Payment.
        boolean paid = payment.pay(payable);
        if (!paid) {
            rollback(reserved);
            throw new IllegalStateException("Payment failed via " + payment.getType());
        }

        // 5. Build the order.
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            orderItems.add(new OrderItem(item.getProduct(), item.getQuantity()));
        }

        Order order = new Order.Builder()
                .id("ORD-" + UUID.randomUUID().toString().substring(0, 8))
                .user(user)
                .items(orderItems)
                .shippingAddress(user.getDefaultAddress())
                .totalAmount(payable)
                .build();

        // Wire up notification observers.
        for (OrderObserver observer : defaultObservers) {
            order.addObserver(observer);
        }

        orders.put(order.getId(), order);
        cartService.clearCart(user.getId());

        // Transition CREATED -> PAID (fires notifications).
        order.advance();
        return order;
    }

    public Order getOrder(String orderId) {
        return orders.get(orderId);
    }

    /** Advance an order through its lifecycle (PAID -> SHIPPED -> DELIVERED). */
    public void advanceOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) throw new IllegalArgumentException("Unknown order " + orderId);
        order.advance();
    }

    public void cancelOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) throw new IllegalArgumentException("Unknown order " + orderId);
        // Return reserved stock to inventory.
        for (OrderItem item : order.getItems()) {
            inventoryService.release(item.getProductId(), item.getQuantity());
        }
        order.cancel();
    }

    private void rollback(List<CartItem> reserved) {
        for (CartItem item : reserved) {
            inventoryService.release(item.getProduct().getId(), item.getQuantity());
        }
    }
}
