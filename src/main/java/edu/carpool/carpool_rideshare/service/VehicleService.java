package edu.carpool.carpool_rideshare.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.carpool.carpool_rideshare.dto.CreateVehicleRequest;
import edu.carpool.carpool_rideshare.entity.User;
import edu.carpool.carpool_rideshare.entity.Vehicle;
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
        User owner =currentUserProvider.getCurrentUser();

        Vehicle vehicle = new Vehicle();
        vehicle.setOwner(owner);
        vehicle.setModel(request.getModel());
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setTotalSeats(request.getTotalSeats());

        return vehicleRepository.save(vehicle);

    }

    public List<Vehicle> getMyVehicls(){
        User owner = currentUserProvider.getCurrentUser();
        return vehicleRepository.findByOwnerId(owner.getId());
    }

    
}
