package edu.carpool.carpool_rideshare.dto;

import jakarta.validation.constraints.NotNull;

public class CreateVehicleRequest {

    private String model;

    @NotNull
    private String licensePlate;

    @NotNull
    private int totalSeats;

    // getters and setters
    public String getModel(){ return this.model; }
    public void setModel( String model) { this.model = model; }

    public String getLicensePlate() { return this.licensePlate; }
    public void setLicensePlate( String licensePlate ) { this.licensePlate = licensePlate; }

    public int getTotalSeats() { return this.totalSeats; }
    public void setTotalSeats( int totalSeats) { this.totalSeats = totalSeats; }
    
}
