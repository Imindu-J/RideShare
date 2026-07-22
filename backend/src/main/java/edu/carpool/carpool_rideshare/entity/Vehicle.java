package edu.carpool.carpool_rideshare.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    private String model;

    @Column(name = "license_plate", nullable = false)
    private String licensePlate;

    @Column(name = "total_seats", nullable = false)
    private int totalSeats;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    //getters and setters

    public Long getId() { return this.id; }

    public User getOwner() { return this.owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public String getModel() { return this.model; }
    public void setModel(String model) { this.model = model; }

    public String getLicensePlate() { return this.licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public Integer getTotalSeats() { return this.totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }

    public LocalDateTime getCreatedAt() { return this.createdAt; }

}
