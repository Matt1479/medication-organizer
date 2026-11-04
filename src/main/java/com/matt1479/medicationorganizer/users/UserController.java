package com.matt1479.medicationorganizer.users;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.matt1479.medicationorganizer.users.dto.ChangeDisplayNameRequest;
import com.matt1479.medicationorganizer.users.dto.ChangeEmailRequest;
import com.matt1479.medicationorganizer.users.dto.ChangePasswordRequest;
import com.matt1479.medicationorganizer.users.dto.RoleRequest;
import com.matt1479.medicationorganizer.users.dto.UserRequest;
import com.matt1479.medicationorganizer.users.dto.UserResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserResponse createUser(@RequestBody @Valid UserRequest request) {
        return userService.createUser(
                request.email(),
                request.displayName(),
                request.password());
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable @Min(1) Long id) {
        return userService.getUserById(id);
    }

    @GetMapping(params = { "email" })
    public UserResponse getUserByEmail(
            @RequestParam @NotBlank @Email @Size(min = 3, max = 254) String email) {
        return userService.getUserByEmail(email);
    }

    @GetMapping(params = { "!email" })
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @PatchMapping("/change-email")
    public UserResponse changeEmail(@RequestBody @Valid ChangeEmailRequest request) {
        return userService.changeEmail(
                // TODO: Replace placeholder ID with the authenticated user's ID
                0L,
                request.currentPassword(),
                request.newEmail());
    }

    @PatchMapping("/change-display-name")
    public UserResponse changeDisplayName(@RequestBody @Valid ChangeDisplayNameRequest request) {
        return userService.changeDisplayName(
                // TODO: Replace placeholder ID with the authenticated user's ID
                0L,
                request.currentPassword(),
                request.newDisplayName());
    }

    @PatchMapping("/change-password")
    public UserResponse changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        return userService.changePassword(
                // TODO: Replace placeholder ID with the authenticated user's ID
                0L,
                request.currentPassword(),
                request.newPassword());
    }

    @PatchMapping("/{id}/enable")
    public UserResponse enableUser(@PathVariable @Min(1) Long id) {
        return userService.enableUser(id);
    }

    @PatchMapping("/{id}/disable")
    public UserResponse disableUser(@PathVariable @Min(1) Long id) {
        return userService.disableUser(id);
    }

    @PostMapping("/{id}/roles")
    public UserResponse addRole(
            @PathVariable @Min(1) Long id,
            @RequestBody @Valid RoleRequest request) {
        return userService.addRole(id, request.role());
    }

    @DeleteMapping("/{id}/roles/{role}")
    public UserResponse removeRole(
            @PathVariable @Min(1) Long id,
            @PathVariable Role role) {
        return userService.removeRole(id, role);
    }

}
