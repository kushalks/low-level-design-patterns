package com.ecommerce.model;

/**
 * Value object representing a shipping/billing address.
 * Immutable - once created it cannot be changed, which makes it safe to share.
 */
public class Address {
    private final String line1;
    private final String city;
    private final String state;
    private final String zipCode;
    private final String country;

    public Address(String line1, String city, String state, String zipCode, String country) {
        this.line1 = line1;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
    }

    public String getCity() { return city; }
    public String getZipCode() { return zipCode; }

    @Override
    public String toString() {
        return line1 + ", " + city + ", " + state + " " + zipCode + ", " + country;
    }
}
