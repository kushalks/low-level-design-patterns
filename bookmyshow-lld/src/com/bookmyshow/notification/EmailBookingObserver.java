package com.bookmyshow.notification;

import com.bookmyshow.model.Booking;

public class EmailBookingObserver implements BookingObserver {
    @Override
    public void onBookingUpdated(Booking booking) {
        System.out.println("    [EMAIL] To " + booking.getUser().getEmail()
                + " -> Booking " + booking.getId() + " is " + booking.getStatus());
    }
}
