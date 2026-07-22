package edu.carpool.carpool_rideshare.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.carpool.carpool_rideshare.dto.CreateVehicleRequest;
import edu.carpool.carpool_rideshare.dto.VehicleResponse;
import edu.carpool.carpool_rideshare.entity.Vehicle;
import edu.carpool.carpool_rideshare.service.VehicleService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users/me/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService){
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody CreateVehicleRequest request){
        Vehicle vehicle = vehicleService.createVehicle(request);
        return ResponseEntity.ok(VehicleResponse.fromEntity(vehicle));
    }

    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getMyVehicles() {
        List<VehicleResponse> vehicles = vehicleService.getMyVehicls().stream()
                    .map(VehicleResponse::fromEntity)
                    .collect(Collectors.toList());
        return ResponseEntity.ok(vehicles);
    }      
    
}
