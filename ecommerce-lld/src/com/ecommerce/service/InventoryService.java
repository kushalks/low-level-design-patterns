package com.ecommerce.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks stock levels per product, separate from the catalog (single
 * responsibility + independent scaling).
 *
 * Concurrency is the interesting part: two customers must not both buy the last
 * unit. We synchronize per-product reservation so the check-and-decrement is
 * atomic. ConcurrentHashMap.compute() gives us an atomic read-modify-write on a
 * single key without locking the whole map.
 */
public class InventoryService {
    private final Map<String, Integer> stock = new ConcurrentHashMap<>();

    public void setStock(String productId, int quantity) {
        stock.put(productId, quantity);
    }

    public int getStock(String productId) {
        return stock.getOrDefault(productId, 0);
    }

    public boolean isAvailable(String productId, int quantity) {
        return getStock(productId) >= quantity;
    }

    /**
     * Atomically reserve stock. Returns false if not enough is available.
     * This is the guard against overselling under concurrent checkouts.
     */
    public boolean reserve(String productId, int quantity) {
        // Holder used to communicate success out of the lambda.
        final boolean[] success = {false};
        stock.compute(productId, (id, current) -> {
            int available = (current == null) ? 0 : current;
            if (available >= quantity) {
                success[0] = true;
                return available - quantity;
            }
            return available; // unchanged
        });
        return success[0];
    }

    /** Release previously reserved stock, e.g. on payment failure or cancellation. */
    public void release(String productId, int quantity) {
        stock.merge(productId, quantity, Integer::sum);
    }
}
