package com.parkinglot.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Physical spot sizes. Each size declares which vehicle types it can hold.
 *
 * Keeping the fit rule here (data-driven via an EnumSet) means adding a new
 * vehicle/spot type is a localized change - no scattered if/else across the code.
 */
public enum SpotType {
    SMALL(EnumSet.of(VehicleType.MOTORCYCLE)),
    MEDIUM(EnumSet.of(VehicleType.MOTORCYCLE, VehicleType.CAR)),
    LARGE(EnumSet.of(VehicleType.MOTORCYCLE, VehicleType.CAR, VehicleType.TRUCK));

    private final Set<VehicleType> allowed;

    SpotType(Set<VehicleType> allowed) {
        this.allowed = allowed;
    }

    public boolean canFit(VehicleType vehicleType) {
        return allowed.contains(vehicleType);
    }
}
