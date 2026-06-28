package com.bookmyshow.service;

/** Thrown when requested seats cannot be reserved (already booked or locked). */
public class SeatUnavailableException extends RuntimeException {
    public SeatUnavailableException(String message) {
        super(message);
    }
}
