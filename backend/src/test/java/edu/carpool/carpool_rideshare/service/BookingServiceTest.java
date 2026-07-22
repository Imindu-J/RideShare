package edu.carpool.carpool_rideshare.service;

import edu.carpool.carpool_rideshare.dto.CreateBookingRequest;
import edu.carpool.carpool_rideshare.entity.*;
import edu.carpool.carpool_rideshare.exception.*;
import edu.carpool.carpool_rideshare.repository.BookingRepository;
import edu.carpool.carpool_rideshare.repository.RideRepository;
import edu.carpool.carpool_rideshare.security.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RideRepository rideRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private BookingService bookingService;

    private User driver;
    private User rider;
    private User otherRider;
    private Vehicle vehicle;
    private Ride ride;
    private Booking booking;

    @BeforeEach
    void setUp() {
        driver = new User();
        driver.setName("Driver One");
        driver.setEmail("driver@test.com");
        ReflectionTestUtils.setField(driver, "id", 1L);

        rider = new User();
        rider.setName("Rider One");
        rider.setEmail("rider@test.com");
        ReflectionTestUtils.setField(rider, "id", 2L);

        otherRider = new User();
        otherRider.setName("Other Rider");
        otherRider.setEmail("other@test.com");
        ReflectionTestUtils.setField(otherRider, "id", 3L);

        vehicle = new Vehicle();
        vehicle.setOwner(driver);
        vehicle.setTotalSeats(4);
        ReflectionTestUtils.setField(vehicle, "id", 100L);

        ride = new Ride();
        ride.setDriver(driver);
        ride.setVehicle(vehicle);
        ride.setOrigin("Library");
        ride.setDestination("North Campus");
        ride.setDepartureTime(LocalDateTime.now().plusDays(1));
        ride.setTotalSeats(3);
        ride.setSeatsAvailable(3);
        ride.setStatus(RideStatus.OFFERED);
        ReflectionTestUtils.setField(ride, "id", 200L);

        booking = new Booking();
        booking.setRide(ride);
        booking.setRider(rider);
        booking.setSeatsRequested(1);
        booking.setStatus(BookingStatus.PENDING);
        ReflectionTestUtils.setField(booking, "id", 300L);
    }

    // 1. driver cannot book their own ride
    void requestBooking_throwsWhenDriverBooksOwnRide() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setSeatsRequested(1);

        when(currentUserProvider.getCurrentUser()).thenReturn(driver);
        when(rideRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> bookingService.requestBooking(200L, request))
                    .isInstanceOf(UnauthorizedActionException.class)
                    .hasMessageContaining("own Ride");

    }


    // 2. cannot book a cancelled or completed ride 
    @Test
    void requestBooking_throwsWhenRideCancelled() {
        ride.setStatus(RideStatus.CANCELLED);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setSeatsRequested(1);

        when(currentUserProvider.getCurrentUser()).thenReturn(rider);
        when(rideRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> bookingService.requestBooking(200L, request))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("CANCELLED");
    }


    // 3. cannot request more seats than available
    @Test
    void requestBooking_throwsWhenSeatsExceedAvailable() {
        ride.setSeatsAvailable(2);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setSeatsRequested(3);

        when(currentUserProvider.getCurrentUser()).thenReturn(rider);
        when(rideRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> bookingService.requestBooking(200L, request))
                .isInstanceOf(SeatUnavailableException.class)
                .hasMessageContaining("Only 2");
    }



    // 4. first booking flips ride from OFFERED to REQUESTED ***
    @Test
    void requestBooking_flipsRideFromOfferedToRequested() {
        assertThat(ride.getStatus()).isEqualTo(RideStatus.OFFERED);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setSeatsRequested(1);

        when(currentUserProvider.getCurrentUser()).thenReturn(rider);
        when(rideRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(ride));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));

        bookingService.requestBooking(200L, request);

        assertThat(ride.getStatus()).isEqualTo(RideStatus.REQUESTED);
    }


    // 5. only the driver can accept a booking on their ride 
    @Test
    void acceptBooking_throwsWhenNotTheDriver() {
        when(currentUserProvider.getCurrentUser()).thenReturn(otherRider);
        when(bookingRepository.findById(300L)).thenReturn(Optional.of(booking));
        when(rideRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> bookingService.acceptBooking(300L))
                .isInstanceOf(UnauthorizedActionException.class)
                .hasMessageContaining("driver");
    }


    // 6. cannot accept a booking that isn't PENDING 
    @Test
    void acceptBooking_throwsWhenBookingNotPending() {
        booking.setStatus(BookingStatus.REJECTED);

        when(currentUserProvider.getCurrentUser()).thenReturn(driver);
        when(bookingRepository.findById(300L)).thenReturn(Optional.of(booking));
        when(rideRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> bookingService.acceptBooking(300L))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("REJECTED");
    }


    // 7. re-check under lock, seats dried up since the booking was made ***
    @Test
    void acceptBooking_throwsWhenSeatsNoLongerAvailable() {
        booking.setSeatsRequested(2);
        ride.setSeatsAvailable(1); // someone else grabbed seats in the meantime

        when(currentUserProvider.getCurrentUser()).thenReturn(driver);
        when(bookingRepository.findById(300L)).thenReturn(Optional.of(booking));
        when(rideRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> bookingService.acceptBooking(300L))
                .isInstanceOf(SeatUnavailableException.class);
    }


    // 8. successful accept decrements seats and confirms ride when full 
    @Test
    void acceptBooking_decrementsSeatsAndFlipsToConfirmedWhenFull() {
        ride.setTotalSeats(1);
        ride.setSeatsAvailable(1);
        booking.setSeatsRequested(1);

        when(currentUserProvider.getCurrentUser()).thenReturn(driver);
        when(bookingRepository.findById(300L)).thenReturn(Optional.of(booking));
        when(rideRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(ride));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.acceptBooking(300L);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.ACCEPTED);
        assertThat(ride.getSeatsAvailable()).isEqualTo(0);
        assertThat(ride.getStatus()).isEqualTo(RideStatus.CONFIRMED);
    }


}
