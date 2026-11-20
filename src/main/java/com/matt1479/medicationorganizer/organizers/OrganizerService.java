package com.matt1479.medicationorganizer.organizers;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.matt1479.medicationorganizer.organizers.dto.CompartmentResponse;
import com.matt1479.medicationorganizer.organizers.dto.OrganizerResponse;

@Service
public class OrganizerService {

    private final OrganizerRepository organizerRepository;

    public OrganizerService(OrganizerRepository organizerRepository) {
        this.organizerRepository = organizerRepository;
    }

    @Transactional
    public OrganizerResponse createOrganizer(
            String productCode,
            String name,
            int daysCovered,
            int compartmentsPerDay,
            String description,
            String qrCode,
            List<Compartment> compartments) {

        productCode = normalizeCode(productCode);
        name = name.trim();

        if (description != null) {
            description = description.isBlank() ? null : description.trim();
        }

        if (qrCode != null) {
            qrCode = qrCode.isBlank() ? null : normalizeCode(qrCode);
        }

        if (organizerRepository.existsByProductCode(productCode)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Product code already exists.");
        }

        if (qrCode != null && organizerRepository.existsByQrCode(qrCode)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "QR code already exists.");
        }

        if (compartments.size() != daysCovered * compartmentsPerDay) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The number of compartments must be equal to daysCovered * compartmentsPerDay.");
        }

        Organizer organizer = new Organizer(
                productCode,
                name,
                daysCovered,
                compartmentsPerDay,
                description,
                qrCode);

        for (Compartment compartment : compartments) {
            if (compartment == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Compartment cannot be null.");
            }

            organizer.addCompartment(compartment);
        }

        organizer.activate();

        Organizer savedOrganizer = organizerRepository.save(organizer);
        return toResponse(savedOrganizer);
    }

    @Transactional(readOnly = true)
    public OrganizerResponse getOrganizerById(Long id) {
        Organizer organizer = organizerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Organizer not found."));

        return toResponse(organizer);
    }

    @Transactional(readOnly = true)
    public OrganizerResponse getOrganizerByProductCode(String productCode) {
        Organizer organizer = organizerRepository.findByProductCode(normalizeCode(productCode))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Organizer not found."));

        return toResponse(organizer);
    }

    @Transactional(readOnly = true)
    public List<OrganizerResponse> getOrganizersByName(String name) {
        List<OrganizerResponse> organizerResponses = new ArrayList<>();

        organizerRepository.findByName(name.trim())
                .forEach((organizer) -> organizerResponses.add(toResponse(organizer)));

        return organizerResponses;
    }

    @Transactional(readOnly = true)
    public OrganizerResponse getOrganizerByQrCode(String qrCode) {
        Organizer organizer = organizerRepository.findByQrCode(normalizeCode(qrCode))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Organizer not found."));

        return toResponse(organizer);
    }

    @Transactional(readOnly = true)
    public List<OrganizerResponse> getAllOrganizers() {
        List<OrganizerResponse> organizerResponses = new ArrayList<>();

        organizerRepository.findAll()
                .forEach((organizer) -> organizerResponses.add(toResponse(organizer)));

        return organizerResponses;
    }

    @Transactional
    public OrganizerResponse changeProductCode(Long id, String newProductCode) {
        newProductCode = normalizeCode(newProductCode);

        Organizer organizer = organizerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Organizer not found."));

        if (organizer.getProductCode().equals(newProductCode)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "New product code must be different than current.");
        }

        if (organizerRepository.existsByProductCode(newProductCode)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "New product code already exists.");
        }

        organizer.setProductCode(newProductCode);

        Organizer updatedOrganizer = organizerRepository.save(organizer);
        return toResponse(updatedOrganizer);
    }

    @Transactional
    public OrganizerResponse changeName(Long id, String newName) {
        newName = newName.trim();

        Organizer organizer = organizerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Organizer not found."));

        if (organizer.getName().equals(newName)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "New name must be different than current.");
        }

        organizer.setName(newName);

        Organizer updatedOrganizer = organizerRepository.save(organizer);
        return toResponse(updatedOrganizer);
    }

    @Transactional
    public OrganizerResponse changeDescription(Long id, String newDescription) {
        if (newDescription != null) {
            newDescription = newDescription.isBlank() ? null : newDescription.trim();
        }

        Organizer organizer = organizerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Organizer not found."));

        if (Objects.equals(organizer.getDescription(), newDescription)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "New description must be different than current.");
        }

        organizer.setDescription(newDescription);

        Organizer updatedOrganizer = organizerRepository.save(organizer);
        return toResponse(updatedOrganizer);
    }

    @Transactional
    public OrganizerResponse changeQrCode(Long id, String newQrCode) {
        if (newQrCode != null) {
            newQrCode = newQrCode.isBlank() ? null : normalizeCode(newQrCode);
        }

        Organizer organizer = organizerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Organizer not found."));

        if (Objects.equals(organizer.getQrCode(), newQrCode)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "New QR code must be different than current.");
        }

        organizer.setQrCode(newQrCode);

        Organizer updatedOrganizer = organizerRepository.save(organizer);
        return toResponse(updatedOrganizer);
    }

    @Transactional
    public OrganizerResponse activateOrganizer(Long id) {
        Organizer organizer = organizerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Organizer not found."));

        if (organizer.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Organizer is already activated.");
        }

        if (!organizer.isComplete()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Organizer is not complete.");
        }

        organizer.activate();

        Organizer updatedOrganizer = organizerRepository.save(organizer);
        return toResponse(updatedOrganizer);
    }

    @Transactional
    public OrganizerResponse deactivateOrganizer(Long id) {
        Organizer organizer = organizerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Organizer not found."));

        if (!organizer.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Organizer is already deactivated.");
        }

        organizer.deactivate();

        Organizer updatedOrganizer = organizerRepository.save(organizer);
        return toResponse(updatedOrganizer);
    }

    @Transactional
    public OrganizerResponse updateCompartment(
            Long organizerId,
            Long compartmentId,
            DayOfWeek dayOfWeek,
            TimeOfDay timeOfDay,
            LocalTime timeFrom,
            LocalTime timeTo) {

        Organizer organizer = organizerRepository.findById(organizerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Organizer not found."));

        Compartment compartment = organizer.getCompartments().stream()
                .filter((c) -> Objects.equals(c.getId(), compartmentId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Compartment not found."));

        compartment.setDayOfWeek(dayOfWeek);
        compartment.setTimeOfDay(timeOfDay);

        compartment.setTime(timeFrom, timeTo);

        Organizer updatedOrganizer = organizerRepository.save(organizer);
        return toResponse(updatedOrganizer);
    }

    private OrganizerResponse toResponse(Organizer organizer) {
        List<CompartmentResponse> compartments = organizer.getCompartments()
                .stream()
                .map((c) -> new CompartmentResponse(
                        c.getId(),
                        c.getPositionIndex(),
                        c.getDayOfWeek(),
                        c.getTimeOfDay(),
                        c.getTimeFrom(),
                        c.getTimeTo()))
                .toList();

        return new OrganizerResponse(
                organizer.getId(),
                organizer.getProductCode(),
                organizer.getName(),
                organizer.getDaysCovered(),
                organizer.getCompartmentsPerDay(),
                organizer.getDescription(),
                organizer.getQrCode(),
                organizer.isActive(),
                organizer.getCreatedAt(),
                organizer.getUpdatedAt(),
                compartments);
    }

    private String normalizeCode(String value) {
        return value.trim().toLowerCase();
    }

}
