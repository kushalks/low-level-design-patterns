package com.ecommerce.state;

import com.ecommerce.model.Order;

/** Payment captured, awaiting fulfilment. */
public class PaidState implements OrderState {

    @Override
    public void next(Order order) {
        // Paid -> Shipped
        order.setState(new ShippedState());
    }

    @Override
    public void cancel(Order order) {
        // Still cancellable before shipment; in a real system this would also
        // trigger a refund workflow.
        order.setState(new CancelledState());
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PAID;
    }
}
