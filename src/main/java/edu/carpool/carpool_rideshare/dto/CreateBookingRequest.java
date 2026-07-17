package edu.carpool.carpool_rideshare.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreateBookingRequest {

    @NotNull
    @Min(1)
    private Integer seatsRequested;

    public Integer getSeatsReqested() { return seatsRequested;}
    public void setSeatsRequested(Integer seatsRequested) { this.seatsRequested = seatsRequested;}

}
