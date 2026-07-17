package edu.carpool.carpool_rideshare.dto;

import java.time.LocalDateTime;

import edu.carpool.carpool_rideshare.entity.Ride;
import edu.carpool.carpool_rideshare.entity.RideStatus;

public class RideResponse {
    
    private Long id;
    private Long driverId;
    private String driverName;
    private Long vehicleId;
    private String origin;
    private String destination;
    private LocalDateTime departureTime;
    private Integer totalSeats;
    private Integer seatsAvailable;
    private RideStatus status;

    public static RideResponse fromEntity(Ride ride) {
        RideResponse res = new RideResponse();
        res.id = ride.getId();
        res.driverId = ride.getDriver().getId();
        res.driverName = ride.getDriver().getName();
        res.vehicleId = ride.getVehicle().getId();
        res.origin = ride.getOrigin();
        res.destination = ride.getDestination();
        res.departureTime = ride.getDepartureTime();
        res.totalSeats = ride.getTotalSeats();
        res.seatsAvailable = ride.getSeatsAvailable();
        res.status = ride.getStatus();
        return res;
    }


    // getters only (read-only response)
    public Long getId() { return id; }
    public Long getDriverId() { return driverId; }
    public String getDriverName() { return driverName; }
    public Long getVehicleId() { return vehicleId; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public Integer getTotalSeats() { return totalSeats; }
    public Integer getSeatsAvailable() { return seatsAvailable; }
    public RideStatus getStatus() { return status; }

}
