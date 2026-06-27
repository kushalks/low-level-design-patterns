package com.ecommerce.service;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Holds the master product catalog and supports browsing / searching.
 *
 * Uses a ConcurrentHashMap so reads (browsing) and writes (catalog updates) are
 * thread-safe - relevant in a multi-threaded server. Search here is a simple
 * in-memory filter; in production this would delegate to Elasticsearch/Solr.
 */
public class ProductCatalogService {
    private final Map<String, Product> products = new ConcurrentHashMap<>();

    public void addProduct(Product product) {
        products.put(product.getId(), product);
    }

    public Optional<Product> getProduct(String productId) {
        return Optional.ofNullable(products.get(productId));
    }

    public List<Product> getAll() {
        return new ArrayList<>(products.values());
    }

    public List<Product> findByCategory(Category category) {
        return products.values().stream()
                .filter(p -> p.getCategory() == category)
                .collect(Collectors.toList());
    }

    /** Case-insensitive keyword search over name and description. */
    public List<Product> search(String keyword) {
        String needle = keyword.toLowerCase();
        return products.values().stream()
                .filter(p -> p.getName().toLowerCase().contains(needle)
                        || p.getDescription().toLowerCase().contains(needle))
                .collect(Collectors.toList());
    }
}
