package com.matt1479.medicationorganizer.organizers;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.matt1479.medicationorganizer.users.User;

public interface UserOrganizerRepository extends CrudRepository<UserOrganizer, Long> {

    Optional<UserOrganizer> findByIdAndUserId(Long id, Long userId);

    Optional<UserOrganizer> findByUserIdAndIsDefaultTrue(Long userId);

    List<UserOrganizer> findByOrganizer(Organizer organizer);

    List<UserOrganizer> findByUser(User user);

}
