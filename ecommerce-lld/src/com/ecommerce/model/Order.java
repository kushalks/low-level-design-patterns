package com.ecommerce.model;

import com.ecommerce.notification.OrderObserver;
import com.ecommerce.state.CreatedState;
import com.ecommerce.state.OrderState;
import com.ecommerce.state.OrderStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Order aggregate. It plays two roles:
 *
 *  1. STATE PATTERN context  - it delegates lifecycle transitions (next/cancel)
 *     to its current OrderState object.
 *  2. OBSERVER PATTERN subject - observers subscribe and are notified whenever
 *     the status changes.
 *
 * Construction is done via the Builder (see Order.Builder) because an order has
 * several required fields and optional ones, and we want it created in a valid,
 * consistent state.
 */
public class Order {
    private final String id;
    private final User user;
    private final List<OrderItem> items;
    private final Address shippingAddress;
    private final double totalAmount;       // amount actually charged (post-discount)

    private OrderState state;
    private final List<OrderObserver> observers = new ArrayList<>();

    private Order(Builder builder) {
        this.id = builder.id;
        this.user = builder.user;
        this.items = builder.items;
        this.shippingAddress = builder.shippingAddress;
        this.totalAmount = builder.totalAmount;
        this.state = new CreatedState();    // every order starts as CREATED
    }

    // ----- State pattern delegation -----

    /** Advance the order to its next state and notify subscribers. */
    public void advance() {
        state.next(this);
        notifyObservers();
    }

    /** Cancel the order (if allowed by the current state) and notify subscribers. */
    public void cancel() {
        state.cancel(this);
        notifyObservers();
    }

    /** Called by the state objects themselves to perform the transition. */
    public void setState(OrderState state) {
        this.state = state;
    }

    public OrderStatus getStatus() {
        return state.getStatus();
    }

    // ----- Observer pattern -----

    public void addObserver(OrderObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(OrderObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        for (OrderObserver observer : observers) {
            observer.onStatusChanged(this);
        }
    }

    // ----- Getters -----

    public String getId() { return id; }
    public User getUser() { return user; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public Address getShippingAddress() { return shippingAddress; }
    public double getTotalAmount() { return totalAmount; }

    @Override
    public String toString() {
        return "Order{" + id + ", items=" + items.size()
                + ", total=$" + totalAmount + ", status=" + getStatus() + "}";
    }

    /**
     * BUILDER PATTERN - assembles an Order step by step and validates before build.
     */
    public static class Builder {
        private String id;
        private User user;
        private List<OrderItem> items = new ArrayList<>();
        private Address shippingAddress;
        private double totalAmount;

        public Builder id(String id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder items(List<OrderItem> items) { this.items = items; return this; }
        public Builder shippingAddress(Address address) { this.shippingAddress = address; return this; }
        public Builder totalAmount(double totalAmount) { this.totalAmount = totalAmount; return this; }

        public Order build() {
            if (id == null || user == null) {
                throw new IllegalStateException("Order requires an id and a user");
            }
            if (items == null || items.isEmpty()) {
                throw new IllegalStateException("Order must contain at least one item");
            }
            return new Order(this);
        }
    }
}
