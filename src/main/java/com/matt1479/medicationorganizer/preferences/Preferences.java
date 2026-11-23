package com.matt1479.medicationorganizer.preferences;

import java.time.Instant;

import com.matt1479.medicationorganizer.users.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table
public class Preferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "simplified_mode", nullable = false)
    private boolean simplifiedMode = false;

    private static final int MIN_TEXT_SCALE = 100;
    private static final int MAX_TEXT_SCALE = 300;

    @Column(name = "text_scale", nullable = false)
    private int textScale = MIN_TEXT_SCALE;

    @Column(name = "high_contrast", nullable = false)
    private boolean highContrast = false;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Preferences() {
    }

    public Preferences(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }

        this.user = user;
    }

    public Preferences(
            User user,
            boolean simplifiedMode,
            int textScale,
            boolean highContrast) {

        this(user);

        if (textScale < MIN_TEXT_SCALE || textScale > MAX_TEXT_SCALE) {
            throw new IllegalArgumentException(
                    "Text scale must be in the range of %d to %d."
                            .formatted(MIN_TEXT_SCALE, MAX_TEXT_SCALE));
        }

        this.simplifiedMode = simplifiedMode;
        this.textScale = textScale;
        this.highContrast = highContrast;
    }

    @Override
    public String toString() {
        return String.format(
                "Preferences[id=%s, simplifiedMode=%s, textScale=%s, highContrast=%s, updatedAt=%s]",
                id, simplifiedMode, textScale, highContrast, updatedAt);
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public boolean isSimplifiedMode() {
        return simplifiedMode;
    }

    public void enableSimplifiedMode() {
        if (isSimplifiedMode()) {
            throw new IllegalStateException("Simplified mode is already enabled.");
        }

        this.simplifiedMode = true;
    }

    public void disableSimplifiedMode() {
        if (!isSimplifiedMode()) {
            throw new IllegalStateException("Simplified mode is already disabled.");
        }

        this.simplifiedMode = false;
    }

    public int getTextScale() {
        return textScale;
    }

    public void setTextScale(int textScale) {
        if (textScale < MIN_TEXT_SCALE || textScale > MAX_TEXT_SCALE) {
            throw new IllegalArgumentException(
                    "Text scale must be in the range of %d to %d."
                            .formatted(MIN_TEXT_SCALE, MAX_TEXT_SCALE));
        }

        this.textScale = textScale;
    }

    public boolean isHighContrast() {
        return highContrast;
    }

    public void enableHighContrast() {
        if (isHighContrast()) {
            throw new IllegalStateException("High contrast is already enabled.");
        }

        this.highContrast = true;
    }

    public void disableHighContrast() {
        if (!isHighContrast()) {
            throw new IllegalStateException("High contrast is already disabled.");
        }

        this.highContrast = false;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

}
