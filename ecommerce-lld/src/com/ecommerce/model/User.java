package com.ecommerce.model;

/**
 * Represents a customer of the platform.
 * Kept as a plain domain entity (POJO) - behaviour lives in the services.
 */
public class User {
    private final String id;
    private final String name;
    private final String email;
    private final String phone;
    private Address defaultAddress;

    public User(String id, String name, String email, String phone, Address defaultAddress) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.defaultAddress = defaultAddress;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Address getDefaultAddress() { return defaultAddress; }
    public void setDefaultAddress(Address defaultAddress) { this.defaultAddress = defaultAddress; }

    @Override
    public String toString() {
        return "User{" + id + ", " + name + "}";
    }
}
