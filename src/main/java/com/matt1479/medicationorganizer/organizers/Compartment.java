package com.matt1479.medicationorganizer.organizers;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "compartments", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "organizer_id", "position_index" })
})
public class Compartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private Organizer organizer;

    @PositiveOrZero
    @Column(name = "position_index", nullable = false)
    private int positionIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_of_day")
    private TimeOfDay timeOfDay;

    @Column(name = "time_from")
    private LocalTime timeFrom;

    @Column(name = "time_to")
    private LocalTime timeTo;

    protected Compartment() {
    }

    public Compartment(
            DayOfWeek dayOfWeek,
            TimeOfDay timeOfDay,
            LocalTime timeFrom,
            LocalTime timeTo) {

        validateTimeRange(timeFrom, timeTo);

        this.dayOfWeek = dayOfWeek;
        this.timeOfDay = timeOfDay;
        this.timeFrom = timeFrom;
        this.timeTo = timeTo;
    }

    @Override
    public String toString() {
        return String.format(
                "Compartment[id=%s, positionIndex=%s, dayOfWeek='%s', timeOfDay='%s', timeFrom='%s', timeTo='%s']",
                id, positionIndex, dayOfWeek, timeOfDay, timeFrom, timeTo);
    }

    @Override
    public int hashCode() {
        // Keep the hash code stable when the database-generated ID is assigned.
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Compartment)) {
            return false;
        }

        Compartment other = (Compartment) o;
        return id != null && id.equals(other.id);
    }

    public Long getId() {
        return id;
    }

    public Organizer getOrganizer() {
        return organizer;
    }

    /*
     * Keep this method package-private to ensure that the relationship
     * is managed through Organizer's addCompartment()
     * and removeCompartment() methods.
     */
    void setOrganizer(Organizer organizer) {
        this.organizer = organizer;
    }

    public int getPositionIndex() {
        return positionIndex;
    }

    void setPositionIndex(int positionIndex) {
        if (positionIndex < 0) {
            throw new IllegalArgumentException("Position index can't be negative.");
        }

        this.positionIndex = positionIndex;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public TimeOfDay getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(TimeOfDay timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    public LocalTime getTimeFrom() {
        return timeFrom;
    }

    public LocalTime getTimeTo() {
        return timeTo;
    }

    public void setTime(LocalTime timeFrom, LocalTime timeTo) {
        validateTimeRange(timeFrom, timeTo);

        this.timeFrom = timeFrom;
        this.timeTo = timeTo;
    }

    private static void validateTimeRange(LocalTime timeFrom, LocalTime timeTo) {
        if (timeFrom != null && timeTo != null && timeFrom.isAfter(timeTo)) {
            throw new IllegalArgumentException("The time range is invalid.");
        }
    }

}
