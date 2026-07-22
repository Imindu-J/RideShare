package edu.carpool.carpool_rideshare.dto;

import edu.carpool.carpool_rideshare.entity.User;
import edu.carpool.carpool_rideshare.entity.Vehicle;

public class VehicleResponse {

    private Long id;
    private User owner;
    private String model;
    private String licensePlate;
    private int totalSeats;

    public static VehicleResponse fromEntity(Vehicle vehicle){
        VehicleResponse res = new VehicleResponse();
        res.id = vehicle.getId();
        res.owner = vehicle.getOwner();
        res.model = vehicle.getModel();
        res.licensePlate = vehicle.getLicensePlate();
        res.totalSeats = vehicle.getTotalSeats();
        return res;
    }

    // getters
    public Long getId() { return id; }
    public User getOwner() { return owner; }
    public String getModel() { return model; }
    public String getLicensePlate() { return licensePlate; }
    public int getTotalSeats() { return totalSeats; }

    
}
