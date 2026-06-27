package com.ecommerce.state;

import com.ecommerce.model.Order;

/**
 * STATE PATTERN.
 *
 * Each concrete state knows which transitions are legal from itself. This avoids
 * a giant switch/if-else block ("state explosion") in the Order class and makes
 * adding a new state (e.g. RETURNED) a closed-for-modification change.
 *
 * The Order delegates lifecycle actions to its current state object.
 */
public interface OrderState {

    /** Move the order forward to its next logical state. */
    void next(Order order);

    /** Attempt to cancel the order. Not all states allow this. */
    void cancel(Order order);

    /** The status this state represents. */
    OrderStatus getStatus();
}
