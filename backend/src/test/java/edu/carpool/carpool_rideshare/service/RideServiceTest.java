package edu.carpool.carpool_rideshare.service;

import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


import edu.carpool.carpool_rideshare.dto.CreateRideRequest;
import edu.carpool.carpool_rideshare.entity.User;
import edu.carpool.carpool_rideshare.entity.Vehicle;
import edu.carpool.carpool_rideshare.exception.UnauthorizedActionException;
import edu.carpool.carpool_rideshare.repository.BookingRepository;
import edu.carpool.carpool_rideshare.repository.RideRepository;
import edu.carpool.carpool_rideshare.repository.VehicleRepository;
import edu.carpool.carpool_rideshare.security.CurrentUserProvider;

@ExtendWith(MockitoExtension.class)
public class RideServiceTest {
    
    @Mock
    private RideRepository rideRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private RideService rideService;

    private User driver;
    private User otherUser;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        driver = new User();
        driver.setName("Driver One");
        driver.setEmail("driver@test.com");
        ReflectionTestUtils.setField(driver, "id", 1L);

        otherUser = new User();
        otherUser.setName("Other User");
        otherUser.setEmail("other@test.com");
        ReflectionTestUtils.setField(otherUser, "id", 2L);

        vehicle = new Vehicle();
        vehicle.setOwner(otherUser);
        vehicle.setTotalSeats(4);
    }

    @Test
    void createRide_ThrowsWhenDriverDoesNotOwnVehicle() {
        CreateRideRequest request = new CreateRideRequest();
        request.setVehicleId(100L);
        request.setOrigin("Library");
        request.setDestination("North Campus");
        request.setDepartureTime(LocalDateTime.now().plusDays(1));
        request.setTotalSeats(3);

        when(currentUserProvider.getCurrentUser()).thenReturn(driver);
        when(vehicleRepository.findById(100L)).thenReturn(Optional.of(vehicle));

        assertThatThrownBy(() -> rideService.createRide(request))
                .isInstanceOf(UnauthorizedActionException.class)
                .hasMessageContaining("your own vehicle");

    }

}
