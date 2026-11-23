package com.matt1479.medicationorganizer.preferences;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.matt1479.medicationorganizer.preferences.dto.PreferencesResponse;
import com.matt1479.medicationorganizer.users.User;
import com.matt1479.medicationorganizer.users.UserRepository;

@Service
public class PreferencesService {

    private final UserRepository userRepository;
    private final PreferencesRepository preferencesRepository;

    public PreferencesService(UserRepository userRepository, PreferencesRepository preferencesRepository) {
        this.userRepository = userRepository;
        this.preferencesRepository = preferencesRepository;
    }

    @Transactional
    public PreferencesResponse createPreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found."));

        preferencesRepository.findByUserId(userId)
                .ifPresent((p) -> {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Preferences already exist for this user.");
                });

        Preferences preferences = new Preferences(user);

        Preferences saved = preferencesRepository.save(preferences);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PreferencesResponse getPreferencesById(Long id) {
        Preferences preferences = preferencesRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Preferences not found."));

        return toResponse(preferences);
    }

    @Transactional(readOnly = true)
    public PreferencesResponse getPreferencesByUserId(Long userId) {
        Preferences preferences = preferencesRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Preferences not found."));

        return toResponse(preferences);
    }

    @Transactional(readOnly = true)
    public PreferencesResponse getPreferencesByIdAndUserId(Long id, Long userId) {
        Preferences found = findPreferences(id, userId);
        return toResponse(found);
    }

    @Transactional(readOnly = true)
    public List<PreferencesResponse> getAllPreferences() {
        List<Preferences> preferences = new ArrayList<>();

        preferencesRepository.findAll()
                .forEach((p) -> preferences.add(p));

        return toResponse(preferences);
    }

    @Transactional
    public void enableSimplifiedMode(Long userId) {
        Preferences found = findPreferences(userId);

        try {
            found.enableSimplifiedMode();
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage());
        }

        preferencesRepository.save(found);
    }

    @Transactional
    public void disableSimplifiedMode(Long userId) {
        Preferences found = findPreferences(userId);

        try {
            found.disableSimplifiedMode();
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage());
        }

        preferencesRepository.save(found);
    }

    @Transactional
    public void changeTextScale(Long userId, int textScale) {
        Preferences found = findPreferences(userId);

        try {
            found.setTextScale(textScale);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage());
        }

        preferencesRepository.save(found);
    }

    @Transactional
    public void enableHighContrast(Long userId) {
        Preferences found = findPreferences(userId);

        try {
            found.enableHighContrast();
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage());
        }

        preferencesRepository.save(found);
    }

    @Transactional
    public void disableHighContrast(Long userId) {
        Preferences found = findPreferences(userId);

        try {
            found.disableHighContrast();
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage());
        }

        preferencesRepository.save(found);
    }

    private PreferencesResponse toResponse(Preferences preferences) {
        return new PreferencesResponse(
                preferences.getId(),
                preferences.getUser().getId(),
                preferences.isSimplifiedMode(),
                preferences.getTextScale(),
                preferences.isHighContrast(),
                preferences.getUpdatedAt());
    }

    private List<PreferencesResponse> toResponse(List<Preferences> preferences) {
        return preferences
                .stream()
                .map((p) -> toResponse(p))
                .toList();
    }

    private Preferences findPreferences(Long userId) {
        return preferencesRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Preferences not found."));
    }

    private Preferences findPreferences(Long id, Long userId) {
        return preferencesRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Preferences not found."));
    }

}
