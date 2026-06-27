package com.ecommerce.state;

import com.ecommerce.model.Order;

/** Terminal failure state. */
public class CancelledState implements OrderState {

    @Override
    public void next(Order order) {
        throw new IllegalStateException("Cannot progress a cancelled order");
    }

    @Override
    public void cancel(Order order) {
        throw new IllegalStateException("Order is already cancelled");
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.CANCELLED;
    }
}
