package com.ecommerce.state;

import com.ecommerce.model.Order;

/** Order has been created but not yet paid. */
public class CreatedState implements OrderState {

    @Override
    public void next(Order order) {
        // Created -> Paid
        order.setState(new PaidState());
    }

    @Override
    public void cancel(Order order) {
        // A created/unpaid order can be freely cancelled.
        order.setState(new CancelledState());
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.CREATED;
    }
}
