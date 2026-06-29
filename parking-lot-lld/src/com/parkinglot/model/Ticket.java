package com.parkinglot.model;

import java.time.Instant;

/** Issued on entry; used on exit to compute the fee. */
public class Ticket {
    private final String id;
    private final Vehicle vehicle;
    private final ParkingSpot spot;
    private final int levelNumber;
    private final Instant entryTime;

    public Ticket(String id, Vehicle vehicle, ParkingSpot spot, int levelNumber, Instant entryTime) {
        this.id = id;
        this.vehicle = vehicle;
        this.spot = spot;
        this.levelNumber = levelNumber;
        this.entryTime = entryTime;
    }

    public String getId() { return id; }
    public Vehicle getVehicle() { return vehicle; }
    public ParkingSpot getSpot() { return spot; }
    public int getLevelNumber() { return levelNumber; }
    public Instant getEntryTime() { return entryTime; }

    @Override
    public String toString() {
        return "Ticket{" + id + ", " + vehicle + ", level " + levelNumber
                + ", spot " + spot.getId() + "}";
    }
}
