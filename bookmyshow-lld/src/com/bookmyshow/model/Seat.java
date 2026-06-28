package com.bookmyshow.model;

/**
 * A physical seat in a screen (e.g. row A, number 5). This is static layout data -
 * it does NOT know whether it is booked. Availability is per-show and lives in
 * ShowSeat, so the same physical seat can be free for one show and booked for another.
 */
public class Seat {
    private final String id;
    private final String row;
    private final int number;
    private final SeatType type;

    public Seat(String id, String row, int number, SeatType type) {
        this.id = id;
        this.row = row;
        this.number = number;
        this.type = type;
    }

    public String getId() { return id; }
    public String getRow() { return row; }
    public int getNumber() { return number; }
    public SeatType getType() { return type; }

    @Override
    public String toString() { return row + number; }
}
