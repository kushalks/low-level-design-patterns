package com.ecommerce.state;

import com.ecommerce.model.Order;

/** Terminal success state. */
public class DeliveredState implements OrderState {

    @Override
    public void next(Order order) {
        throw new IllegalStateException("Order is already delivered - terminal state");
    }

    @Override
    public void cancel(Order order) {
        throw new IllegalStateException("Cannot cancel a delivered order");
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.DELIVERED;
    }
}
