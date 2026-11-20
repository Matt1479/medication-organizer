package com.matt1479.medicationorganizer.organizers;

import java.time.Instant;

import com.matt1479.medicationorganizer.users.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "user_organizers")
public class UserOrganizer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private Organizer organizer;

    private String displayName;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean isDefault = false;

    @NotNull
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    protected UserOrganizer() {
    }

    public UserOrganizer(User user, Organizer organizer) {
        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }

        if (organizer == null) {
            throw new IllegalArgumentException("Organizer is required.");
        }

        this.user = user;
        this.organizer = organizer;
    }

    public UserOrganizer(User user, Organizer organizer, String displayName) {
        this(user, organizer);

        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return String.format(
                "UserOrganizer[id=%s, displayName='%s', active=%s, isDefault=%s, assignedAt=%s]",
                id, displayName, active, isDefault, assignedAt);
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Organizer getOrganizer() {
        return organizer;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isActive() {
        return active;
    }

    public void activate() {
        if (isActive()) {
            throw new IllegalStateException("User organizer is already active.");
        }

        this.active = true;
    }

    public void deactivate() {
        if (!isActive()) {
            throw new IllegalStateException("User organizer is already inactive.");
        }

        this.active = false;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void makeDefault() {
        if (isDefault()) {
            throw new IllegalStateException("User organizer is already the default.");
        }

        this.isDefault = true;
    }

    public void removeDefault() {
        if (!isDefault()) {
            throw new IllegalStateException("User organizer is not the default.");
        }

        this.isDefault = false;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    @PrePersist
    protected void onCreate() {
        assignedAt = Instant.now();
    }

}
