package com.bookmyshow.model;

import java.util.Collections;
import java.util.List;

/** A cinema in a city. Contains one or more screens. */
public class Theatre {
    private final String id;
    private final String name;
    private final City city;
    private final List<Screen> screens;

    public Theatre(String id, String name, City city, List<Screen> screens) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.screens = screens;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public City getCity() { return city; }
    public List<Screen> getScreens() { return Collections.unmodifiableList(screens); }

    @Override
    public String toString() { return name + " (" + city + ")"; }
}
