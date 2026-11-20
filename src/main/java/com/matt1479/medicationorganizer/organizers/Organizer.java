package com.matt1479.medicationorganizer.organizers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "organizers")
public class Organizer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "product_code", nullable = false, unique = true)
    private String productCode;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Min(1)
    @Column(name = "days_covered", nullable = false)
    private int daysCovered;

    @Min(1)
    @Column(name = "compartments_per_day", nullable = false)
    private int compartmentsPerDay;

    private String description;

    @Column(name = "qr_code", unique = true)
    private String qrCode;

    @Column(nullable = false)
    private boolean active = false;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(
            // Compartment owns the relationship
            mappedBy = "organizer", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("positionIndex ASC")
    private List<Compartment> compartments = new ArrayList<>();

    protected Organizer() {
    }

    public Organizer(
            String productCode,
            String name,
            int daysCovered,
            int compartmentsPerDay) {

        if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException("Product code is required.");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }

        if (daysCovered <= 0) {
            throw new IllegalArgumentException("Days covered must be a positive integer.");
        }

        if (compartmentsPerDay <= 0) {
            throw new IllegalArgumentException("Compartments per day must be a positive integer.");
        }

        this.productCode = productCode;
        this.name = name;
        this.daysCovered = daysCovered;
        this.compartmentsPerDay = compartmentsPerDay;
    }

    public Organizer(
            String productCode,
            String name,
            int daysCovered,
            int compartmentsPerDay,
            String description,
            String qrCode) {

        this(productCode, name, daysCovered, compartmentsPerDay);

        this.description = description;
        this.qrCode = qrCode;
    }

    @Override
    public String toString() {
        return String.format(
                "Organizer[id=%s, productCode='%s', name='%s', daysCovered=%s, compartmentsPerDay=%s, description='%s', qrCode='%s', active=%s, createdAt=%s, updatedAt=%s]",
                id, productCode, name, daysCovered, compartmentsPerDay, description, qrCode, active, createdAt,
                updatedAt);
    }

    public Long getId() {
        return id;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException("Product code is required.");
        }

        this.productCode = productCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }

        this.name = name;
    }

    public int getDaysCovered() {
        return daysCovered;
    }

    public void setDaysCovered(int daysCovered) {
        if (active) {
            throw new IllegalStateException(
                    "Deactivate organizer before changing its capacity.");
        }

        if (daysCovered <= 0) {
            throw new IllegalArgumentException("Days covered must be a positive integer.");
        }

        this.daysCovered = daysCovered;
    }

    public int getCompartmentsPerDay() {
        return compartmentsPerDay;
    }

    public void setCompartmentsPerDay(int compartmentsPerDay) {
        if (active) {
            throw new IllegalStateException(
                    "Deactivate organizer before changing its capacity.");
        }

        if (compartmentsPerDay <= 0) {
            throw new IllegalArgumentException("Compartments per day must be a positive integer.");
        }

        this.compartmentsPerDay = compartmentsPerDay;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public boolean isActive() {
        return active;
    }

    public void activate() {
        if (!isComplete()) {
            throw new IllegalStateException(
                    "Organizer must be complete before activation.");
        }

        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<Compartment> getCompartments() {
        return List.copyOf(compartments);
    }

    public void addCompartment(Compartment compartment) {
        if (active) {
            throw new IllegalStateException(
                    "Deactivate organizer before modifying its compartments.");
        }

        if (compartment == null) {
            throw new IllegalArgumentException("Compartment is required.");
        }

        if (compartments.contains(compartment)) {
            return;
        }

        if (compartment.getOrganizer() != null && compartment.getOrganizer() != this) {
            throw new IllegalArgumentException(
                    "Compartment already belongs to another organizer.");
        }

        if (compartments.size() >= getCapacity()) {
            throw new IllegalStateException("Organizer is full.");
        }

        compartment.setPositionIndex(getNextPosition());

        compartments.add(compartment);
        compartment.setOrganizer(this);
    }

    public void removeCompartment(Compartment compartment) {
        if (active) {
            throw new IllegalStateException(
                    "Deactivate organizer before modifying its compartments.");
        }

        if (compartments.remove(compartment)) {
            compartment.setOrganizer(null);
        }
    }

    public boolean isComplete() {
        return compartments.size() == getCapacity();
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    private int getNextPosition() {
        return compartments.stream()
                .mapToInt((compartment) -> compartment.getPositionIndex())
                .max()
                .orElse(-1) + 1;
    }

    private int getCapacity() {
        return daysCovered * compartmentsPerDay;
    }

}
