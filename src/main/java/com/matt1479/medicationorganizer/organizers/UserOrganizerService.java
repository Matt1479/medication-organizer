package com.matt1479.medicationorganizer.organizers;

import com.matt1479.medicationorganizer.users.User;
import com.matt1479.medicationorganizer.users.UserRepository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.matt1479.medicationorganizer.organizers.dto.UserOrganizerResponse;

@Service
public class UserOrganizerService {

    private final UserOrganizerRepository userOrganizerRepository;
    private final UserRepository userRepository;
    private final OrganizerRepository organizerRepository;

    public UserOrganizerService(
            UserOrganizerRepository userOrganizerRepository,
            UserRepository userRepository,
            OrganizerRepository organizerRepository) {

        this.userOrganizerRepository = userOrganizerRepository;
        this.userRepository = userRepository;
        this.organizerRepository = organizerRepository;
    }

    @Transactional
    public UserOrganizerResponse createUserOrganizer(
            Long userId,
            Long organizerId,
            String displayName) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found."));

        Organizer organizer = organizerRepository.findById(organizerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Organizer not found."));

        displayName = normalize(displayName);

        UserOrganizer userOrganizer = new UserOrganizer(user, organizer, displayName);

        UserOrganizer saved = userOrganizerRepository.save(userOrganizer);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserOrganizerResponse getUserOrganizerById(Long id) {
        UserOrganizer userOrganizer = userOrganizerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User organizer not found."));

        return toResponse(userOrganizer);
    }

    @Transactional(readOnly = true)
    public List<UserOrganizerResponse> getUserOrganizersByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found."));

        List<UserOrganizer> userOrganizers = userOrganizerRepository.findByUser(user);
        return toResponse(userOrganizers);
    }

    @Transactional(readOnly = true)
    public List<UserOrganizerResponse> getUserOrganizersByOrganizerId(Long organizerId) {
        Organizer organizer = organizerRepository.findById(organizerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Organizer not found."));

        List<UserOrganizer> userOrganizers = userOrganizerRepository.findByOrganizer(organizer);
        return toResponse(userOrganizers);
    }

    @Transactional(readOnly = true)
    public List<UserOrganizerResponse> getAllUserOrganizers() {
        List<UserOrganizer> userOrganizers = new ArrayList<>();

        userOrganizerRepository.findAll()
                .forEach((userOrganizer) -> userOrganizers.add(userOrganizer));

        return toResponse(userOrganizers);
    }

    @Transactional
    public void changeDisplayName(Long id, String displayName) {
        UserOrganizer userOrganizer = userOrganizerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User organizer not found."));

        displayName = normalize(displayName);

        userOrganizer.setDisplayName(displayName);
        userOrganizerRepository.save(userOrganizer);
    }

    @Transactional
    public void activateUserOrganizer(Long id) {
        UserOrganizer userOrganizer = userOrganizerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User organizer not found."));

        if (userOrganizer.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "User organizer is already activated.");
        }

        userOrganizer.activate();
        userOrganizerRepository.save(userOrganizer);
    }

    @Transactional
    public void deactivateUserOrganizer(Long id) {
        UserOrganizer userOrganizer = userOrganizerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User organizer not found."));

        if (!userOrganizer.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "User organizer is already deactivated.");
        }

        if (userOrganizer.isDefault()) {
            userOrganizer.removeDefault();
        }

        userOrganizer.deactivate();
        userOrganizerRepository.save(userOrganizer);
    }

    @Transactional
    public void makeDefault(Long userId, Long userOrganizerId) {
        UserOrganizer found = findUserOrganizer(userId, userOrganizerId);

        if (!found.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cannot make an inactive organizer the default.");
        }

        if (found.isDefault()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "User organizer is already the default.");
        }

        userOrganizerRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent((userOrganizer) -> userOrganizer.removeDefault());

        found.makeDefault();
        userOrganizerRepository.save(found);
    }

    @Transactional
    public void removeDefault(Long userId, Long userOrganizerId) {
        UserOrganizer found = findUserOrganizer(userId, userOrganizerId);

        if (!found.isDefault()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "User organizer is not the default.");
        }

        found.removeDefault();
        userOrganizerRepository.save(found);
    }

    private UserOrganizerResponse toResponse(UserOrganizer userOrganizer) {
        return new UserOrganizerResponse(
                userOrganizer.getId(),
                userOrganizer.getUser().getId(),
                userOrganizer.getOrganizer().getId(),
                userOrganizer.getDisplayName(),
                userOrganizer.isActive(),
                userOrganizer.isDefault(),
                userOrganizer.getAssignedAt());
    }

    private List<UserOrganizerResponse> toResponse(List<UserOrganizer> userOrganizers) {
        return userOrganizers
                .stream()
                .map((userOrganizer) -> toResponse(userOrganizer))
                .toList();
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private UserOrganizer findUserOrganizer(Long userId, Long userOrganizerId) {
        return userOrganizerRepository.findByIdAndUserId(userOrganizerId, userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User organizer not found."));
    }

}
