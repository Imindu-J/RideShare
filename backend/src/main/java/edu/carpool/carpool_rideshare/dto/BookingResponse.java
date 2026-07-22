package edu.carpool.carpool_rideshare.dto;

import java.time.LocalDateTime;

import edu.carpool.carpool_rideshare.entity.Booking;
import edu.carpool.carpool_rideshare.entity.BookingStatus;

public class BookingResponse {

    private Long id;
    private Long rideId;
    private Long riderId;
    private String riderName;
    private Integer seatsRequested;
    private BookingStatus status;
    private LocalDateTime createdAt;

    public static BookingResponse fromEntity(Booking booking) {
        BookingResponse res = new BookingResponse();
        res.id = booking.getId();
        res.rideId = booking.getRide().getId();
        res.riderId = booking.getRider().getId();
        res.riderName = booking.getRider().getName();
        res.seatsRequested = booking.getSeatsRequested();
        res.status = booking.getStatus();
        res.createdAt = booking.getCreatedAt();
        return res;
    }

    public Long getId() { return id; }
    public Long getRideId() { return rideId; }
    public Long getRiderId() { return riderId; }
    public String getRiderName() { return riderName; }
    public Integer getSeatsRequested() { return seatsRequested; }
    public BookingStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
 
}
