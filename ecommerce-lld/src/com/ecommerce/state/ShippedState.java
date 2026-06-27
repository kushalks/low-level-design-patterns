package com.ecommerce.state;

import com.ecommerce.model.Order;

/** Order is in transit. */
public class ShippedState implements OrderState {

    @Override
    public void next(Order order) {
        // Shipped -> Delivered
        order.setState(new DeliveredState());
    }

    @Override
    public void cancel(Order order) {
        // Once shipped it can no longer be cancelled - only returned (a separate flow).
        throw new IllegalStateException("Cannot cancel an order that has already shipped");
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.SHIPPED;
    }
}
