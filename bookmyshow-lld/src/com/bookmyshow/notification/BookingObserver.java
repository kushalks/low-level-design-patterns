package com.bookmyshow.notification;

import com.bookmyshow.model.Booking;

/**
 * OBSERVER PATTERN. Channels (email/SMS/push) subscribe to booking lifecycle
 * events. The BookingService notifies them without knowing the concrete channels.
 */
public interface BookingObserver {
    void onBookingUpdated(Booking booking);
}
