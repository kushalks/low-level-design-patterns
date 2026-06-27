package com.ecommerce.model;

/**
 * A sellable item in the catalog.
 * Note: stock is intentionally NOT stored here - inventory is managed separately
 * by InventoryService so that product metadata and stock levels can scale and
 * be cached independently (single responsibility).
 */
public class Product {
    private final String id;
    private final String name;
    private final String description;
    private final double price;
    private final Category category;

    public Product(String id, String name, String description, double price, Category category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public Category getCategory() { return category; }

    @Override
    public String toString() {
        return name + " ($" + price + ")";
    }
}
