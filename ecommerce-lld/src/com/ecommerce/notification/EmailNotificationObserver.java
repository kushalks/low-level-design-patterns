package com.ecommerce.notification;

import com.ecommerce.model.Order;

/** Sends an email whenever an order's status changes. */
public class EmailNotificationObserver implements OrderObserver {

    @Override
    public void onStatusChanged(Order order) {
        System.out.println("  [EMAIL] To " + order.getUser().getEmail()
                + " -> Order " + order.getId() + " is now " + order.getStatus());
    }
}
