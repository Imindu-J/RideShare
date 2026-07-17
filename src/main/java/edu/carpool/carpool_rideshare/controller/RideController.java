package edu.carpool.carpool_rideshare.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.carpool.carpool_rideshare.dto.CreateRideRequest;
import edu.carpool.carpool_rideshare.dto.RideResponse;
import edu.carpool.carpool_rideshare.entity.Ride;
import edu.carpool.carpool_rideshare.entity.RideStatus;
import edu.carpool.carpool_rideshare.service.RideService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/rides")
public class RideController {
    
    private final RideService rideService;

    public RideController(RideService rideService){
        this.rideService = rideService;
    }

    @PostMapping
    public ResponseEntity<RideResponse> createRide(@Valid @RequestBody CreateRideRequest request){
        Ride ride = rideService.createRide(request);
        return ResponseEntity.ok(RideResponse.fromEntity(ride));
    }

    @GetMapping
    public ResponseEntity<List<RideResponse>> getRides(@RequestParam(required = false) RideStatus status){
        List<RideResponse> rides = rideService.getRidesByStatus(status).stream()
                    .map(RideResponse::fromEntity)
                    .collect(Collectors.toList());
        return ResponseEntity.ok(rides);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RideResponse> getRide(@PathVariable Long id) {
        return ResponseEntity.ok(RideResponse.fromEntity(rideService.getRideById(id)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<RideResponse> cancelRide(@PathVariable Long id) {
        return ResponseEntity.ok(RideResponse.fromEntity(rideService.cancelRide(id)));
    }
    
}
