package com.matt1479.medicationorganizer.preferences;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface PreferencesRepository extends CrudRepository<Preferences, Long> {

    Optional<Preferences> findByIdAndUserId(Long id, Long userId);

    Optional<Preferences> findByUserId(Long userId);

}
