package com.ecommerce;

import com.ecommerce.discount.DiscountStrategy;
import com.ecommerce.discount.PercentageDiscount;
import com.ecommerce.model.Address;
import com.ecommerce.model.Category;
import com.ecommerce.model.Order;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.notification.EmailNotificationObserver;
import com.ecommerce.notification.OrderObserver;
import com.ecommerce.notification.SmsNotificationObserver;
import com.ecommerce.payment.PaymentFactory;
import com.ecommerce.payment.PaymentStrategy;
import com.ecommerce.payment.PaymentType;
import com.ecommerce.service.CartService;
import com.ecommerce.service.InventoryService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductCatalogService;

import java.util.Arrays;
import java.util.List;

/**
 * End-to-end demo that exercises the whole design. Run this to walk an
 * interviewer through the flow.
 */
public class Main {
    public static void main(String[] args) {
        // ---- Bootstrap services (manual dependency injection) ----
        ProductCatalogService catalog = new ProductCatalogService();
        InventoryService inventory = new InventoryService();
        CartService cartService = new CartService(inventory);

        List<OrderObserver> observers = Arrays.asList(
                new EmailNotificationObserver(),
                new SmsNotificationObserver());
        OrderService orderService = new OrderService(inventory, cartService, observers);

        // ---- Seed catalog + inventory ----
        Product laptop = new Product("P1", "UltraBook 14", "Lightweight laptop", 1200.0, Category.ELECTRONICS);
        Product phone  = new Product("P2", "Pixel Phone", "Android flagship", 800.0, Category.ELECTRONICS);
        Product book   = new Product("P3", "Clean Code", "Robert C. Martin", 40.0, Category.BOOKS);
        catalog.addProduct(laptop);
        catalog.addProduct(phone);
        catalog.addProduct(book);
        inventory.setStock("P1", 5);
        inventory.setStock("P2", 2);
        inventory.setStock("P3", 10);

        // ---- A user browses & searches ----
        User alice = new User("U1", "Alice", "alice@example.com", "+1-555-0100",
                new Address("1 Market St", "San Francisco", "CA", "94105", "USA"));

        System.out.println("=== Browse ELECTRONICS ===");
        catalog.findByCategory(Category.ELECTRONICS).forEach(p -> System.out.println("  " + p));

        System.out.println("\n=== Search 'clean' ===");
        catalog.search("clean").forEach(p -> System.out.println("  " + p));

        // ---- Add to cart ----
        System.out.println("\n=== Add to cart ===");
        cartService.addToCart(alice.getId(), laptop, 1);
        cartService.addToCart(alice.getId(), book, 2);
        System.out.println("  Cart subtotal: $" + cartService.getOrCreateCart(alice.getId()).getSubtotal());

        // ---- Checkout: 10% discount (max $150), pay by credit card ----
        System.out.println("\n=== Checkout ===");
        DiscountStrategy discount = new PercentageDiscount(10, 150);
        PaymentStrategy payment = PaymentFactory.create(PaymentType.CREDIT_CARD, "4111111111111234");
        Order order = orderService.placeOrder(alice, discount, payment);
        System.out.println("  Placed: " + order);

        // ---- Fulfilment lifecycle (State pattern + Observer notifications) ----
        System.out.println("\n=== Fulfilment ===");
        orderService.advanceOrder(order.getId()); // PAID -> SHIPPED
        orderService.advanceOrder(order.getId()); // SHIPPED -> DELIVERED
        System.out.println("  Final: " + order);

        // ---- Inventory after sale ----
        System.out.println("\n=== Inventory left ===");
        System.out.println("  Laptop: " + inventory.getStock("P1") + ", Book: " + inventory.getStock("P3"));

        // ---- Demonstrate an illegal transition is rejected ----
        System.out.println("\n=== Illegal transition guard ===");
        try {
            orderService.cancelOrder(order.getId()); // delivered -> cannot cancel
        } catch (IllegalStateException e) {
            System.out.println("  Rejected as expected: " + e.getMessage());
        }
    }
}
