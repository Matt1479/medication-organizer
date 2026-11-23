package com.matt1479.medicationorganizer.preferences;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;


import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.matt1479.medicationorganizer.preferences.dto.UpdateTextScaleRequest;
import com.matt1479.medicationorganizer.preferences.dto.PreferencesResponse;
import com.matt1479.medicationorganizer.security.CurrentUser;

@RestController
@RequestMapping("/api/preferences")
@Validated
public class PreferencesController {

    private final PreferencesService preferencesService;
    private final CurrentUser currentUser;

    public PreferencesController(PreferencesService preferencesService, CurrentUser currentUser) {
        this.preferencesService = preferencesService;
        this.currentUser = currentUser;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PreferencesResponse createPreferences() {
        return preferencesService.createPreferences(currentUser.getId());
    }

    @GetMapping("/{id}")
    public PreferencesResponse getPreferencesById(@PathVariable @Min(1) Long id) {
        return preferencesService.getPreferencesById(id);
    }

    @GetMapping("/me")
    public PreferencesResponse getMyPreferences() {
        return preferencesService.getPreferencesByUserId(currentUser.getId());
    }

    @GetMapping(path = "/{id}", params = { "userId" })
    public PreferencesResponse getPreferencesByIdAndUserId(
            @PathVariable @Min(1) Long id,
            @RequestParam @Min(1) Long userId) {

        return preferencesService.getPreferencesByIdAndUserId(id, userId);
    }

    @GetMapping
    public List<PreferencesResponse> getAllPreferences() {
        return preferencesService.getAllPreferences();
    }

    @PatchMapping("/me/simplified-mode")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enableSimplifiedMode() {
        preferencesService.enableSimplifiedMode(currentUser.getId());
    }

    @DeleteMapping("/me/simplified-mode")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disableSimplifiedMode() {
        preferencesService.disableSimplifiedMode(currentUser.getId());
    }

    @PatchMapping("/me/text-scale")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeTextScale(@RequestBody @Valid UpdateTextScaleRequest request) {
        preferencesService.changeTextScale(
                currentUser.getId(),
                request.textScale());
    }

    @PatchMapping("/me/high-contrast")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enableHighContrast() {
        preferencesService.enableHighContrast(currentUser.getId());
    }

    @DeleteMapping("/me/high-contrast")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disableHighContrast() {
        preferencesService.disableHighContrast(currentUser.getId());
    }

}
