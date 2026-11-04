package com.matt1479.medicationorganizer.users;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.matt1479.medicationorganizer.users.dto.UserResponse;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Transactional
    public UserResponse createUser(String email, String displayName, String rawPassword) {
        email = email.trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Email already exists");
        }

        displayName = displayName == null ? null : displayName.trim();

        String passwordHash = encoder.encode(rawPassword);
        User user = new User(email, displayName, passwordHash);

        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        email = email.trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        List<UserResponse> userResponses = new ArrayList<>();

        userRepository.findAll()
                .forEach((user) -> userResponses.add(toResponse(user)));

        return userResponses;
    }

    @Transactional
    public UserResponse changeEmail(Long id, String currentRawPassword, String newEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        newEmail = newEmail.trim().toLowerCase();

        if (!encoder.matches(currentRawPassword, user.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Password is incorrect");
        }

        if (user.getEmail().equals(newEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "New email must be different than current");
        }

        if (userRepository.existsByEmail(newEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Email already exists");
        }

        // TODO: Verify email
        user.setEmail(newEmail);
        // TODO: Invalidate existing token

        User updatedUser = userRepository.save(user);
        return toResponse(updatedUser);
    }

    @Transactional
    public UserResponse changeDisplayName(Long id, String currentRawPassword, String newDisplayName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        newDisplayName = newDisplayName == null ? null : newDisplayName.trim();

        if (!encoder.matches(currentRawPassword, user.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Password is incorrect");
        }

        if (Objects.equals(user.getDisplayName(), newDisplayName)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "New displayName must be different than current");
        }

        user.setDisplayName(newDisplayName);

        User updatedUser = userRepository.save(user);
        return toResponse(updatedUser);
    }

    @Transactional
    public UserResponse changePassword(Long id, String currentRawPassword, String newRawPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        if (!encoder.matches(currentRawPassword, user.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Password is incorrect");
        }

        if (currentRawPassword.equals(newRawPassword)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "New password must be different than current");
        }

        user.setPasswordHash(encoder.encode(newRawPassword));
        // TODO: Invalidate existing token

        User updatedUser = userRepository.save(user);
        return toResponse(updatedUser);
    }

    @Transactional
    public UserResponse enableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        if (user.isEnabled()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "User is already enabled");
        }

        user.enable();

        User updatedUser = userRepository.save(user);
        return toResponse(updatedUser);
    }

    @Transactional
    public UserResponse disableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        if (!user.isEnabled()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "User is already disabled");
        }

        user.disable();

        User updatedUser = userRepository.save(user);
        return toResponse(updatedUser);
    }

    @Transactional
    public UserResponse addRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        user.addRole(role);

        User updatedUser = userRepository.save(user);
        return toResponse(updatedUser);
    }

    @Transactional
    public UserResponse removeRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        user.removeRole(role);

        User updatedUser = userRepository.save(user);
        return toResponse(updatedUser);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getRoles());
    }

}
