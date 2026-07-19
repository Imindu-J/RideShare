package edu.carpool.carpool_rideshare;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

import edu.carpool.carpool_rideshare.dto.BookingResponse;
import edu.carpool.carpool_rideshare.dto.RideResponse;
import edu.carpool.carpool_rideshare.dto.VehicleResponse;
import edu.carpool.carpool_rideshare.dto.AuthResponse;


@AutoConfigureTestRestTemplate
public class BookingConcurrencyIntegrationTest extends IntegrationTestBase{

    @Autowired  
    private TestRestTemplate restTemplate;

    @Test
    void concurrentAccepts_onlyFillsAvailableSeats_neverOversells() throws InterruptedException, ExecutionException, TimeoutException{
        // driver, vehicle and ride with only 2 seats

        String driverToken = registerAndGetToken("conc-driver@test.com", Set.of("DRIVER"));
        HttpHeaders driverHeaders = authHeaders(driverToken);

        Map<String, Object> vehicleReq = Map.of(
            "model", "Corolla",
            "licensePlate", "Conc-" + System.currentTimeMillis(),
            "totalSeats", 5
        );

        VehicleResponse vehicle = restTemplate.postForEntity(
            "/api/users/me/vehicles", new HttpEntity<>(vehicleReq, driverHeaders), VehicleResponse.class).getBody();

        Map<String, Object> rideReq = Map.of(
            "vehicleId", vehicle.getId(),
            "origin", "Library",
            "destination", "North Campus",
            "departureTime", LocalDateTime.now().plusDays(1).toString(),
            "totalSeats", 2
        );

        RideResponse ride = restTemplate.postForEntity(
            "/api/rides", new HttpEntity<>(rideReq, driverHeaders), RideResponse.class).getBody();
        Long rideId = ride.getId();

        // 5 riders create a pending booking for 1 seat

        int riderCount = 5;
        List<Long> bookingIds = new ArrayList<>();

        for(int i = 0; i < riderCount; i++){
            String riderToken = registerAndGetToken("conc-rider" + i + "@test.com", Set.of("RIDER"));
            HttpHeaders riderHeaders = authHeaders(riderToken);

            Map<String, Object> bookingReq = Map.of("seatsRequested", 1);
            BookingResponse booking = restTemplate.postForEntity(
                "/api/rides/" + rideId + "/bookings", new HttpEntity<> (bookingReq, riderHeaders), BookingResponse.class).getBody();

            bookingIds.add(booking.getId());
        }

        // fire all 5 accept requests at once
        ExecutorService executor = Executors.newFixedThreadPool(riderCount);
        CountDownLatch readyLatch = new CountDownLatch(riderCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (Long bookingId : bookingIds) {
            futures.add(executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();  // all threads block here untill released together
                    ResponseEntity<String> res = restTemplate.exchange(
                        "/api/bookings/" + bookingId + "/accept", 
                        HttpMethod.PATCH,
                        new HttpEntity<>(driverHeaders),
                        String.class);
                    if (res.getStatusCode().is2xxSuccessful()){
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                }catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                }
            }));
        }

        readyLatch.await(); // wait untill all 5 threats are quesd up and ready
        startLatch.countDown();  //release them all at the same moment
    
        for (Future<?> f : futures) {
            f.get(10, TimeUnit.SECONDS);
        }
        executor.shutdown();

        // Assertions: exactly 2 succeed, exactly 3 fail (409 Conflict)
        assertThat(successCount.get()).isEqualTo(2);
        assertThat(failureCount.get()).isEqualTo(3);
    
        // rides seat count 0
        RideResponse finalRide = restTemplate.exchange(
                "/api/rides/" + rideId, HttpMethod.GET, new HttpEntity<>(driverHeaders), RideResponse.class).getBody();
        assertThat(finalRide.getSeatsAvailable()).isEqualTo(0);
        assertThat(finalRide.getStatus().toString()).isEqualTo("CONFIRMED");
    }

    private String registerAndGetToken(String email, Set<String> roles) {
        Map<String, Object> req = Map.of(
                "name", "Test " + email,
                "email", email,
                "password", "password123",
                "roles", roles
        );
        AuthResponse auth = restTemplate.postForEntity("/api/auth/register", req, AuthResponse.class).getBody();
        return auth.getToken();
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

}
    

