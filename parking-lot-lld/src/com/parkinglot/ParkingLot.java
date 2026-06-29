package com.parkinglot;

import com.parkinglot.fee.FeeStrategy;
import com.parkinglot.model.ParkingLevel;
import com.parkinglot.model.ParkingSpot;
import com.parkinglot.model.Ticket;
import com.parkinglot.model.Vehicle;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Facade over the whole lot: park (issue ticket) and unpark (compute fee, free spot).
 *
 * Tries each level in order and parks in the first fitting spot. The actual
 * race-safety lives in ParkingSpot.park() (synchronized), so this orchestration
 * stays simple. Active tickets are tracked in a ConcurrentHashMap.
 */
public class ParkingLot {
    private final List<ParkingLevel> levels;
    private final FeeStrategy feeStrategy;
    private final Map<String, Ticket> activeTickets = new ConcurrentHashMap<>();

    public ParkingLot(List<ParkingLevel> levels, FeeStrategy feeStrategy) {
        this.levels = levels;
        this.feeStrategy = feeStrategy;
    }

    /** Park a vehicle. Returns a Ticket, or null if the lot is full for its type. */
    public Ticket park(Vehicle vehicle) {
        for (ParkingLevel level : levels) {
            ParkingSpot spot = level.parkVehicle(vehicle);
            if (spot != null) {
                Ticket ticket = new Ticket(
                        "TKT-" + UUID.randomUUID().toString().substring(0, 8),
                        vehicle, spot, level.getLevelNumber(), Instant.now());
                activeTickets.put(ticket.getId(), ticket);
                return ticket;
            }
        }
        return null; // full
    }

    /** Exit: free the spot and return the fee. */
    public double unpark(String ticketId, Instant exitTime) {
        Ticket ticket = activeTickets.remove(ticketId);
        if (ticket == null) {
            throw new IllegalArgumentException("Invalid or already-used ticket: " + ticketId);
        }
        ticket.getSpot().vacate();
        return feeStrategy.calculate(ticket, exitTime);
    }

    public long totalFreeSpots() {
        return levels.stream().mapToLong(ParkingLevel::countFree).sum();
    }
}
