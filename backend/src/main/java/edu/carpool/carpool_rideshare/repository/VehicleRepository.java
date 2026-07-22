package edu.carpool.carpool_rideshare.repository;

import edu.carpool.carpool_rideshare.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByOwnerId(Long ownerId);
}
