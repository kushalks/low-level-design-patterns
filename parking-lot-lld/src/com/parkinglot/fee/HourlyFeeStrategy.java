package com.parkinglot.fee;

import com.parkinglot.model.Ticket;
import com.parkinglot.model.VehicleType;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

/** Charges per started hour, at a rate that depends on the vehicle type. */
public class HourlyFeeStrategy implements FeeStrategy {
    private final Map<VehicleType, Double> hourlyRate = new EnumMap<>(VehicleType.class);

    public HourlyFeeStrategy() {
        hourlyRate.put(VehicleType.MOTORCYCLE, 10.0);
        hourlyRate.put(VehicleType.CAR, 20.0);
        hourlyRate.put(VehicleType.TRUCK, 40.0);
    }

    @Override
    public double calculate(Ticket ticket, Instant exitTime) {
        long minutes = Duration.between(ticket.getEntryTime(), exitTime).toMinutes();
        long hours = Math.max(1, (long) Math.ceil(minutes / 60.0)); // min 1 hour
        return hours * hourlyRate.get(ticket.getVehicle().getType());
    }
}
