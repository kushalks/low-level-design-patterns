package com.ecommerce.notification;

import com.ecommerce.model.Order;

/** Sends an SMS whenever an order's status changes. */
public class SmsNotificationObserver implements OrderObserver {

    @Override
    public void onStatusChanged(Order order) {
        System.out.println("  [SMS]   To " + order.getUser().getPhone()
                + " -> Order " + order.getId() + " is now " + order.getStatus());
    }
}
