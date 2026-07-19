package edu.carpool.carpool_rideshare.dto;

import edu.carpool.carpool_rideshare.entity.User;
import jakarta.validation.constraints.NotNull;

public class CreateVehicleRequest {

    @NotNull
    private Long vehicleId;

    @NotNull
    private User owner;

    private String model;

    @NotNull
    private String licensePlate;

    @NotNull
    private int totalSeats;

    // getters and setters
    public Long getVehicleId() { return this.vehicleId; }
    public void setVehicleId(Long id){ this.vehicleId = id; }

    public User getOwner() { return this.owner; }
    public void setOwner( User owner) { this.owner = owner; }

    public String getModel(){ return this.model; }
    public void setModel( String model) { this.model = model; }

    public String getLicensePlate() { return this.licensePlate; }
    public void setLicensePlate( String licensePlate ) { this.licensePlate = licensePlate; }

    public int getTotalSeats() { return this.totalSeats; }
    public void setTotalSeats( int totalSeats) { this.totalSeats = totalSeats; }
    
}
