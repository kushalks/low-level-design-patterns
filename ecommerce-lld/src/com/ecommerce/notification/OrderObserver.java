package com.ecommerce.notification;

import com.ecommerce.model.Order;

/**
 * OBSERVER PATTERN - observer side.
 *
 * Anyone interested in order lifecycle changes (email, SMS, analytics, warehouse
 * system) implements this interface and subscribes to the Order. The Order does
 * not need to know the concrete observers - it just notifies them. This keeps
 * notification channels decoupled and open for extension.
 */
public interface OrderObserver {
    void onStatusChanged(Order order);
}
