package com.matt1479.medicationorganizer.organizers;

import java.util.List;

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

import com.matt1479.medicationorganizer.organizers.dto.CreateUserOrganizerRequest;
import com.matt1479.medicationorganizer.organizers.dto.UpdateDisplayNameRequest;
import com.matt1479.medicationorganizer.organizers.dto.UserOrganizerResponse;
import com.matt1479.medicationorganizer.security.CurrentUser;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/user-organizers")
@Validated
public class UserOrganizerController {

    private final UserOrganizerService userOrganizerService;
    private final CurrentUser currentUser;

    public UserOrganizerController(UserOrganizerService userOrganizerService, CurrentUser currentUser) {
        this.userOrganizerService = userOrganizerService;
        this.currentUser = currentUser;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserOrganizerResponse createUserOrganizer(@RequestBody @Valid CreateUserOrganizerRequest request) {
        return userOrganizerService.createUserOrganizer(
                currentUser.getId(),
                request.organizerId(),
                request.displayName());
    }

    @GetMapping("/{id}")
    public UserOrganizerResponse getUserOrganizerById(@PathVariable @Min(1) Long id) {
        return userOrganizerService.getUserOrganizerById(id);
    }

    @GetMapping("/me")
    public List<UserOrganizerResponse> getMyUserOrganizers() {
        return userOrganizerService.getUserOrganizersByUserId(currentUser.getId());
    }

    @GetMapping(params = { "organizerId" })
    public List<UserOrganizerResponse> getUserOrganizersByOrganizerId(@RequestParam @Min(1) Long organizerId) {
        return userOrganizerService.getUserOrganizersByOrganizerId(organizerId);
    }

    @GetMapping(params = { "!organizerId" })
    public List<UserOrganizerResponse> getAllUserOrganizers() {
        return userOrganizerService.getAllUserOrganizers();
    }

    @PatchMapping("/{id}/display-name")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeDisplayName(
            @PathVariable @Min(1) Long id,
            @RequestBody @Valid UpdateDisplayNameRequest request) {

        userOrganizerService.changeDisplayName(id, request.displayName());
    }

    @PatchMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activateUserOrganizer(@PathVariable @Min(1) Long id) {
        userOrganizerService.activateUserOrganizer(id);
    }

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateUserOrganizer(@PathVariable @Min(1) Long id) {
        userOrganizerService.deactivateUserOrganizer(id);
    }

    @PatchMapping("/me/{userOrganizerId}/default")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void makeDefault(@PathVariable @Min(1) Long userOrganizerId) {
        userOrganizerService.makeDefault(currentUser.getId(), userOrganizerId);
    }

    @DeleteMapping("/me/{userOrganizerId}/default")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeDefault(@PathVariable @Min(1) Long userOrganizerId) {
        userOrganizerService.removeDefault(currentUser.getId(), userOrganizerId);
    }

}
