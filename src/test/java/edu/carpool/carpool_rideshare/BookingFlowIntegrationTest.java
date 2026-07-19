package edu.carpool.carpool_rideshare;

import edu.carpool.carpool_rideshare.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@AutoConfigureTestRestTemplate
class BookingFlowIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void fullBookingFlow_registerToAccept() {
        // Register driver
        Map<String, Object> driverReq = Map.of(
                "name", "IT Driver",
                "email", "it-driver@test.com",
                "password", "password123",
                "roles", Set.of("DRIVER")
        );
        ResponseEntity<AuthResponse> driverAuth = restTemplate.postForEntity("/api/auth/register", driverReq, AuthResponse.class);
        assertThat(driverAuth.getStatusCode()).isEqualTo(HttpStatus.OK);
        String driverToken = driverAuth.getBody().getToken();

        // Create vehicle
        HttpHeaders driverHeaders = new HttpHeaders();
        driverHeaders.setBearerAuth(driverToken);
        Map<String, Object> vehicleReq = Map.of(
                "model", "Corolla",
                "licensePlate", "IT-" + System.currentTimeMillis(),
                "totalSeats", 4
        );
        ResponseEntity<VehicleResponse> vehicleRes = restTemplate.postForEntity(
                "/api/users/me/vehicles", new HttpEntity<>(vehicleReq, driverHeaders), VehicleResponse.class);
        assertThat(vehicleRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        Long vehicleId = vehicleRes.getBody().getId();

        // Create ride
        Map<String, Object> rideReq = Map.of(
                "vehicleId", vehicleId,
                "origin", "Library",
                "destination", "North Campus",
                "departureTime", LocalDateTime.now().plusDays(1).toString(),
                "totalSeats", 2
        );
        ResponseEntity<RideResponse> rideRes = restTemplate.postForEntity(
                "/api/rides", new HttpEntity<>(rideReq, driverHeaders), RideResponse.class);
        assertThat(rideRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        Long rideId = rideRes.getBody().getId();
        assertThat(rideRes.getBody().getSeatsAvailable()).isEqualTo(2);

        // Register rider
        Map<String, Object> riderReq = Map.of(
                "name", "IT Rider",
                "email", "it-rider@test.com",
                "password", "password123",
                "roles", Set.of("RIDER")
        );
        ResponseEntity<AuthResponse> riderAuth = restTemplate.postForEntity("/api/auth/register", riderReq, AuthResponse.class);
        String riderToken = riderAuth.getBody().getToken();
        HttpHeaders riderHeaders = new HttpHeaders();
        riderHeaders.setBearerAuth(riderToken);

        // Request booking
        Map<String, Object> bookingReq = Map.of("seatsRequested", 1);
        ResponseEntity<BookingResponse> bookingRes = restTemplate.postForEntity(
                "/api/rides/" + rideId + "/bookings", new HttpEntity<>(bookingReq, riderHeaders), BookingResponse.class);
        assertThat(bookingRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(bookingRes.getBody().getStatus().toString()).isEqualTo("PENDING");
        Long bookingId = bookingRes.getBody().getId();

        // Driver accepts
        ResponseEntity<BookingResponse> acceptRes = restTemplate.exchange(
                "/api/bookings/" + bookingId + "/accept",
                org.springframework.http.HttpMethod.PATCH,
                new HttpEntity<>(driverHeaders),
                BookingResponse.class);
        assertThat(acceptRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(acceptRes.getBody().getStatus().toString()).isEqualTo("ACCEPTED");

        // Verify ride seat count dropped
        ResponseEntity<RideResponse> finalRide = restTemplate.exchange(
                "/api/rides/" + rideId,
                org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(driverHeaders),
                RideResponse.class);
        assertThat(finalRide.getBody().getSeatsAvailable()).isEqualTo(1);
        assertThat(finalRide.getBody().getStatus().toString()).isEqualTo("REQUESTED");
    }
}
