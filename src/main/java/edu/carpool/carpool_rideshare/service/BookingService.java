package edu.carpool.carpool_rideshare.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.carpool.carpool_rideshare.dto.CreateBookingRequest;
import edu.carpool.carpool_rideshare.entity.Booking;
import edu.carpool.carpool_rideshare.entity.BookingStatus;
import edu.carpool.carpool_rideshare.entity.Ride;
import edu.carpool.carpool_rideshare.entity.RideStatus;
import edu.carpool.carpool_rideshare.entity.User;
import edu.carpool.carpool_rideshare.exception.InvalidStatusTransitionException;
import edu.carpool.carpool_rideshare.exception.ResourceNotFoundException;
import edu.carpool.carpool_rideshare.exception.SeatUnavailableException;
import edu.carpool.carpool_rideshare.exception.UnauthorizedActionException;
import edu.carpool.carpool_rideshare.repository.BookingRepository;
import edu.carpool.carpool_rideshare.repository.RideRepository;
import edu.carpool.carpool_rideshare.security.CurrentUserProvider;
import jakarta.transaction.Transactional;

@Service
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final RideRepository rideRepository;
    private final CurrentUserProvider currentUserProvider;

    public BookingService(BookingRepository bookingRepository, RideRepository rideRepository, CurrentUserProvider currentUserProvider){
        this.bookingRepository = bookingRepository;
        this.rideRepository = rideRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public Booking requestBooking(Long rideId, CreateBookingRequest request){
        User rider = currentUserProvider.getCurrentUser();

        //Pessimistic lock
        Ride ride = rideRepository.findByIdForUpdate(rideId).orElseThrow(() -> new ResourceNotFoundException("Ride not found"));

        if (ride.getDriver().getId().equals(rider.getId())){
            throw new UnauthorizedActionException("You cannot book your own ride.");
        }

        if (ride.getStatus() == RideStatus.CANCELLED || ride.getStatus() == RideStatus.COMPLETED){
            throw new InvalidStatusTransitionException("Cannot book a ride that is " + ride.getStatus());
        }

        int seatsRequested = request.getSeatsReqested();

        if (seatsRequested > ride.getSeatsAvailable()){
            throw new SeatUnavailableException("Only " + ride.getSeatsAvailable() + " seat(s) available. " + seatsRequested + " seats requested.");
        }

        Booking booking = new Booking();
        booking.setRide(ride);
        booking.setRider(rider);
        booking.setSeatsRequested(seatsRequested);
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking); 
        
        // Ride status changes OFFERED -> REQUESTED when its first booking request comes in
        if (ride.getStatus() == RideStatus.OFFERED){
            ride.setStatus(RideStatus.REQUESTED);
            rideRepository.save(ride);
        }

        return booking;

    }

    @Transactional
    public Booking acceptBooking(Long bookingId){
        User driver = currentUserProvider.getCurrentUser();

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found."));

        Ride ride = rideRepository.findByIdForUpdate(booking.getRide().getId()).orElseThrow(() -> new ResourceNotFoundException("Ride not found."));

        if (!ride.getDriver().getId().equals(driver.getId())){
            throw new UnauthorizedActionException("Only the driver can accept bookings.");
        }

        if (booking.getStatus() != BookingStatus.PENDING){
            throw new InvalidStatusTransitionException(
                    "Cannot accept a booking that is " + booking.getStatus());
        }

        // recheck seat availability to prevent overbooking
        if (booking.getSeatsRequested() > ride.getSeatsAvailable()) {
            throw new SeatUnavailableException(
                    "Not enough seats remaining to accept this booking");
        }

        booking.setStatus(BookingStatus.ACCEPTED);
        ride.setSeatsAvailable(ride.getSeatsAvailable() - booking.getSeatsRequested());

        if (ride.getSeatsAvailable() == 0) {
            ride.setStatus(RideStatus.CONFIRMED);
        }

        bookingRepository.save(booking);
        rideRepository.save(ride);

        return booking;
    }

    @Transactional
    public Booking rejectBooking(Long bookingId){
        User driver = currentUserProvider.getCurrentUser();
        
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));;

        if (!booking.getRide().getDriver().getId().equals(driver.getId())) {
            throw new UnauthorizedActionException("Only the driver can reject bookings on this ride");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                    "Cannot reject a booking that is " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.REJECTED);
        return bookingRepository.save(booking);

    }

    @Transactional
    public Booking cancelBooking(Long bookingId){
        User rider = currentUserProvider.getCurrentUser();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getRider().getId().equals(rider.getId())) {
            throw new UnauthorizedActionException("You can only cancel your own bookings");
        }

        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.ACCEPTED) {
            throw new InvalidStatusTransitionException(
                    "Cannot cancel a booking that is " + booking.getStatus());
        }

        boolean wasAccepted = booking.getStatus() == BookingStatus.ACCEPTED;
        booking.setStatus(BookingStatus.CANCELLED);

        if(wasAccepted){
            Ride ride = rideRepository.findByIdForUpdate(booking.getRide().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ride not found."));

            ride.setSeatsAvailable(ride.getSeatsAvailable() + booking.getSeatsRequested());

            // CONFIRMED -> REQUESTED if a 
            if (ride.getStatus() == RideStatus.CONFIRMED){
                ride.setStatus(RideStatus.REQUESTED);
            }

            rideRepository.save(ride);
        }

        return booking;

    }

    public List<Booking> getBookingsForRide(Long rideId){
        return bookingRepository.findByRideId(rideId);
    }

    public List<Booking> getMyBookingHistory(){
        User curretUser = currentUserProvider.getCurrentUser();
        return bookingRepository.findByRiderId(curretUser.getId());
    }

}
