package edu.carpool.carpool_rideshare.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.carpool.carpool_rideshare.dto.CreateRideRequest;
import edu.carpool.carpool_rideshare.entity.BookingStatus;
import edu.carpool.carpool_rideshare.entity.Ride;
import edu.carpool.carpool_rideshare.entity.RideStatus;
import edu.carpool.carpool_rideshare.entity.User;
import edu.carpool.carpool_rideshare.entity.Vehicle;
import edu.carpool.carpool_rideshare.exception.InvalidStatusTransitionException;
import edu.carpool.carpool_rideshare.exception.ResourceNotFoundException;
import edu.carpool.carpool_rideshare.exception.UnauthorizedActionException;
import edu.carpool.carpool_rideshare.repository.BookingRepository;
import edu.carpool.carpool_rideshare.repository.RideRepository;
import edu.carpool.carpool_rideshare.repository.VehicleRepository;
import edu.carpool.carpool_rideshare.security.CurrentUserProvider;
import jakarta.transaction.Transactional;

@Service
public class RideService {
    
    private final RideRepository rideRepository;
    private final VehicleRepository vehicleRepository;
    private final BookingRepository bookingRepository;
    private final CurrentUserProvider currentUserProvider;

    public RideService(RideRepository rideRepository, VehicleRepository vehicleRepository, BookingRepository bookingRepository, CurrentUserProvider currentUserProvider){
        this.rideRepository = rideRepository;
        this.vehicleRepository = vehicleRepository;
        this.bookingRepository = bookingRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public Ride createRide(CreateRideRequest request){
        User driver = currentUserProvider.getCurrentUser();
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId()).orElseThrow(() -> new ResourceNotFoundException("Vehicle not foundd"));

        if (!vehicle.getOwner().getId().equals(driver.getId())){
            throw new UnauthorizedActionException("You can only offer rides using your own vehicles.");
        }

        if (request.getTotalSeats() > vehicle.getTotalSeats()){
            throw new IllegalArgumentException("Ride seat count cannot exceed vehicle seats");
        }

        Ride ride = new Ride();
        ride.setDriver(driver);
        ride.setVehicle(vehicle);
        ride.setOrigin(request.getOrigin());
        ride.setDestination(request.getDestination());
        ride.setDepartureTime(request.getDepartureTime());
        ride.setTotalSeats(request.getTotalSeats());
        ride.setSeatsAvailable(request.getTotalSeats());
        ride.setStatus(RideStatus.OFFERED);

        return rideRepository.save(ride);
    }

    public List<Ride> getRidesByStatus(RideStatus status){
        return status != null ? rideRepository.findByStatus(status) : rideRepository.findAll(); 
    }

    public Ride getRideById(Long id){
        return rideRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Ride not found"));
    }

    @Transactional
    public Ride cancelRide(Long rideId){
        User currentUser = currentUserProvider.getCurrentUser();
        Ride ride = rideRepository.findByIdForUpdate(rideId).orElseThrow(() -> new ResourceNotFoundException("Ride not found"));

        if (!ride.getDriver().getId().equals(currentUser.getId())){
            throw new UnauthorizedActionException("Only the driver can cance this ride");
        }

        if (ride.getStatus().equals(RideStatus.COMPLETED) || ride.getStatus().equals(RideStatus.CANCELLED )){
            throw new InvalidStatusTransitionException("Cannot cancel a ride that is alreade " + ride.getStatus());
        }

        ride.setStatus(RideStatus.CANCELLED);

        // auto reject any pending bookings on a cancelled ride
        bookingRepository.findByRideId(rideId).stream()
                .filter(b->b.getStatus() == BookingStatus.PENDING)
                .forEach(b->b.setStatus(BookingStatus.REJECTED));

        return rideRepository.save(ride);
    }

}
