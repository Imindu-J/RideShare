package edu.carpool.carpool_rideshare.service;

import org.springframework.stereotype.Service;

import edu.carpool.carpool_rideshare.dto.CreateVehicleRequest;
import edu.carpool.carpool_rideshare.entity.User;
import edu.carpool.carpool_rideshare.entity.Vehicle;
import edu.carpool.carpool_rideshare.exception.UnauthorizedActionException;
import edu.carpool.carpool_rideshare.repository.VehicleRepository;
import edu.carpool.carpool_rideshare.security.CurrentUserProvider;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CurrentUserProvider currentUserProvider;


    public VehicleService(VehicleRepository vehicleRepository, CurrentUserProvider currentUserProvider){
        this.vehicleRepository = vehicleRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public Vehicle createVehicle(CreateVehicleRequest request){
        User owner = request.getOwner();

        if (!owner.getId().equals(currentUserProvider.getCurrentUser().getId())){
            throw new UnauthorizedActionException("You can only list your own vehicles.");
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setOwner(owner);
        vehicle.setModel(request.getModel());
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setTotalSeats(request.getTotalSeats());

        return vehicleRepository.save(vehicle);

    }

    
}
