package com.parkinglot;

import com.parkinglot.fee.FeeStrategy;
import com.parkinglot.fee.HourlyFeeStrategy;
import com.parkinglot.model.ParkingLevel;
import com.parkinglot.model.ParkingSpot;
import com.parkinglot.model.SpotType;
import com.parkinglot.model.Ticket;
import com.parkinglot.model.Vehicle;
import com.parkinglot.model.VehicleType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** Runnable demo: build a 3-level lot, park a few vehicles, then unpark with fees. */
public class Main {
    public static void main(String[] args) {
        // ---- Build a lot with 3 levels, each with a small mix of spots ----
        List<ParkingLevel> levels = new ArrayList<>();
        for (int lvl = 1; lvl <= 3; lvl++) {
            levels.add(new ParkingLevel(lvl, buildSpots(lvl)));
        }
        FeeStrategy feeStrategy = new HourlyFeeStrategy();
        ParkingLot lot = new ParkingLot(levels, feeStrategy);

        System.out.println("Lot ready with 3 levels. Free spots: " + lot.totalFreeSpots());

        // ---- Park a few vehicles of different types ----
        System.out.println("\n=== Parking ===");
        Ticket t1 = lot.park(new Vehicle("KA-01-MOTO", VehicleType.MOTORCYCLE));
        Ticket t2 = lot.park(new Vehicle("KA-02-CAR", VehicleType.CAR));
        Ticket t3 = lot.park(new Vehicle("KA-03-TRUCK", VehicleType.TRUCK));
        System.out.println("  " + t1);
        System.out.println("  " + t2);
        System.out.println("  " + t3);
        System.out.println("  Free spots now: " + lot.totalFreeSpots());

        // ---- Unpark and show the fee (pretend the car stayed ~2h 10m) ----
        System.out.println("\n=== Exit ===");
        Instant exit = t2.getEntryTime().plus(130, ChronoUnit.MINUTES);
        double fee = lot.unpark(t2.getId(), exit);
        System.out.println("  " + t2.getVehicle() + " parked 130 min -> fee Rs." + fee + " (3 hrs x Rs.20)");
        System.out.println("  Free spots now: " + lot.totalFreeSpots());

        // ---- Show that the lot can fill up / reject ----
        System.out.println("\n=== Lot full handling ===");
        int parked = 0;
        while (lot.park(new Vehicle("EXTRA-TRUCK-" + parked, VehicleType.TRUCK)) != null) {
            parked++;
        }
        System.out.println("  Parked " + parked + " more trucks until LARGE spots ran out; "
                + "next truck rejected (park returned null).");
    }

    /**
     * Each level: 2 SMALL, 2 MEDIUM, 1 LARGE. IDs like L1-S1, L1-M2, L1-L1.
     */
    private static List<ParkingSpot> buildSpots(int level) {
        List<ParkingSpot> spots = new ArrayList<>();
        spots.add(new ParkingSpot("L" + level + "-S1", SpotType.SMALL));
        spots.add(new ParkingSpot("L" + level + "-S2", SpotType.SMALL));
        spots.add(new ParkingSpot("L" + level + "-M1", SpotType.MEDIUM));
        spots.add(new ParkingSpot("L" + level + "-M2", SpotType.MEDIUM));
        spots.add(new ParkingSpot("L" + level + "-L1", SpotType.LARGE));
        return spots;
    }
}
