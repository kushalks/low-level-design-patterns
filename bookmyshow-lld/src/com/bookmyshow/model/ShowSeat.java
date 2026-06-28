package com.bookmyshow.model;

/**
 * The availability + price of one physical Seat for one specific Show.
 *
 * Why a separate object? The same seat A1 is independent across shows - free for
 * the 6pm show, booked for the 9pm show. Pricing also varies per show (e.g.
 * weekend surcharge), so price is captured here, not on Seat.
 */
public class ShowSeat {
    private final Seat seat;
    private final double price;
    private volatile SeatStatus status;

    public ShowSeat(Seat seat, double price) {
        this.seat = seat;
        this.price = price;
        this.status = SeatStatus.AVAILABLE;
    }

    public Seat getSeat() { return seat; }
    public double getPrice() { return price; }
    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }
}
