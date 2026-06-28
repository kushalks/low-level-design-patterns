package com.bookmyshow.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A specific screening: a Movie on a Screen at a start time. Owns the per-show
 * seat map (seatId -> ShowSeat) which carries live availability and price.
 */
public class Show {
    private final String id;
    private final Movie movie;
    private final Screen screen;
    private final LocalDateTime startTime;
    private final Map<String, ShowSeat> showSeats; // keyed by physical seatId

    public Show(String id, Movie movie, Screen screen, LocalDateTime startTime,
                Map<String, ShowSeat> showSeats) {
        this.id = id;
        this.movie = movie;
        this.screen = screen;
        this.startTime = startTime;
        this.showSeats = showSeats;
    }

    public String getId() { return id; }
    public Movie getMovie() { return movie; }
    public Screen getScreen() { return screen; }
    public LocalDateTime getStartTime() { return startTime; }

    public ShowSeat getShowSeat(String seatId) {
        return showSeats.get(seatId);
    }

    public Map<String, ShowSeat> getShowSeats() {
        return Collections.unmodifiableMap(showSeats);
    }

    /** Convenience for the UI: seats currently marked AVAILABLE (ignores transient locks). */
    public List<String> getAvailableSeatIds() {
        return showSeats.entrySet().stream()
                .filter(e -> e.getValue().getStatus() == SeatStatus.AVAILABLE)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return movie.getTitle() + " @ " + screen.getName() + " " + startTime;
    }
}
