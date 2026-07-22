package edu.carpool.carpool_rideshare.controller;

import edu.carpool.carpool_rideshare.dto.BookingResponse;
import edu.carpool.carpool_rideshare.dto.CreateBookingRequest;
import edu.carpool.carpool_rideshare.entity.Booking;
import edu.carpool.carpool_rideshare.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/rides/{rideId}/bookings")
    public ResponseEntity<BookingResponse> requestBooking(@PathVariable Long rideId,
                                                           @Valid @RequestBody CreateBookingRequest request) {
        Booking booking = bookingService.requestBooking(rideId, request);
        return ResponseEntity.ok(BookingResponse.fromEntity(booking));
    }

    @PatchMapping("/bookings/{id}/accept")
    public ResponseEntity<BookingResponse> accept(@PathVariable Long id) {
        return ResponseEntity.ok(BookingResponse.fromEntity(bookingService.acceptBooking(id)));
    }

    @PatchMapping("/bookings/{id}/reject")
    public ResponseEntity<BookingResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(BookingResponse.fromEntity(bookingService.rejectBooking(id)));
    }

    @PatchMapping("/bookings/{id}/cancel")
    public ResponseEntity<BookingResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(BookingResponse.fromEntity(bookingService.cancelBooking(id)));
    }

    @GetMapping("/rides/{rideId}/bookings")
    public ResponseEntity<List<BookingResponse>> getForRide(@PathVariable Long rideId) {
        List<BookingResponse> bookings = bookingService.getBookingsForRide(rideId).stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/users/me/bookings")
    public ResponseEntity<List<BookingResponse>> myHistory() {
        List<BookingResponse> bookings = bookingService.getMyBookingHistory().stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
    }
}
