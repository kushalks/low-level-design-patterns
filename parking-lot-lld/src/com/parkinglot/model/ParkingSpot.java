package com.parkinglot.model;

/**
 * A single parking spot. Knows its size and which vehicle (if any) occupies it.
 *
 * park()/vacate() are synchronized so two threads can't grab the same spot - the
 * core correctness guard for concurrent parking.
 */
public class ParkingSpot {
    private final String id;
    private final SpotType type;
    private Vehicle vehicle; // null when free

    public ParkingSpot(String id, SpotType type) {
        this.id = id;
        this.type = type;
    }

    public String getId() { return id; }
    public SpotType getType() { return type; }

    public boolean isFree() {
        return vehicle == null;
    }

    /** True if this spot is free and big enough for the vehicle. */
    public boolean canPark(Vehicle vehicle) {
        return isFree() && type.canFit(vehicle.getType());
    }

    /** Atomically claim the spot. Returns false if it was taken or doesn't fit. */
    public synchronized boolean park(Vehicle vehicle) {
        if (!canPark(vehicle)) {
            return false;
        }
        this.vehicle = vehicle;
        return true;
    }

    public synchronized void vacate() {
        this.vehicle = null;
    }

    @Override
    public String toString() {
        return id + "(" + type + ", " + (isFree() ? "free" : vehicle.getLicensePlate()) + ")";
    }
}
