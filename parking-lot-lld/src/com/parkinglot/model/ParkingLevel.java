package com.parkinglot.model;

import java.util.List;

/**
 * One level/floor of the lot. Holds a list of spots and can find + claim the
 * first one that fits a vehicle.
 */
public class ParkingLevel {
    private final int levelNumber;
    private final List<ParkingSpot> spots;

    public ParkingLevel(int levelNumber, List<ParkingSpot> spots) {
        this.levelNumber = levelNumber;
        this.spots = spots;
    }

    public int getLevelNumber() { return levelNumber; }

    /**
     * Find the first fitting free spot and claim it atomically. We rely on
     * ParkingSpot.park() (synchronized) to win the race if two cars target the
     * same spot - we just move on to the next spot if park() returns false.
     */
    public ParkingSpot parkVehicle(Vehicle vehicle) {
        for (ParkingSpot spot : spots) {
            if (spot.canPark(vehicle) && spot.park(vehicle)) {
                return spot;
            }
        }
        return null; // level full for this vehicle type
    }

    public long countFree() {
        return spots.stream().filter(ParkingSpot::isFree).count();
    }
}
