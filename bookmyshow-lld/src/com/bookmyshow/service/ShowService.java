package com.bookmyshow.service;

import com.bookmyshow.model.Movie;
import com.bookmyshow.model.Screen;
import com.bookmyshow.model.Seat;
import com.bookmyshow.model.Show;
import com.bookmyshow.model.ShowSeat;
import com.bookmyshow.pricing.PricingStrategy;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Creates and stores shows. On creation it builds the per-show seat map by pricing
 * every physical seat through the injected PricingStrategy.
 */
public class ShowService {
    private final Map<String, Show> shows = new ConcurrentHashMap<>();

    public Show createShow(Movie movie, Screen screen, LocalDateTime startTime,
                           PricingStrategy pricingStrategy) {
        Map<String, ShowSeat> showSeats = new LinkedHashMap<>();
        for (Seat seat : screen.getSeats()) {
            double price = pricingStrategy.priceFor(seat.getType());
            showSeats.put(seat.getId(), new ShowSeat(seat, price));
        }
        Show show = new Show("SHOW-" + UUID.randomUUID().toString().substring(0, 6),
                movie, screen, startTime, showSeats);
        shows.put(show.getId(), show);
        return show;
    }

    public Show getShow(String showId) {
        return shows.get(showId);
    }

    /** Find shows for a given movie (a real impl would also filter by city/date). */
    public List<Show> findShowsByMovie(String movieId) {
        return shows.values().stream()
                .filter(s -> s.getMovie().getId().equals(movieId))
                .collect(Collectors.toList());
    }
}
