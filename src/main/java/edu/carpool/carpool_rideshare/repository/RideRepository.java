package edu.carpool.carpool_rideshare.repository;

import edu.carpool.carpool_rideshare.entity.Ride;
import edu.carpool.carpool_rideshare.entity.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;



public interface RideRepository extends JpaRepository<Ride, Long>{
    
    List<Ride> findByStatus(RideStatus status);

    //Pessimistic lock for seat booking
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Ride r WHERE r.id = :id")
    Optional<Ride> findByIdForUpdate(@Param("id") Long id);

}
