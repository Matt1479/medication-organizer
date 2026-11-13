package com.matt1479.medicationorganizer.organizers;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import java.util.List;


public interface OrganizerRepository extends CrudRepository<Organizer, Long> {

    Optional<Organizer> findByProductCode(String productCode);

    boolean existsByProductCode(String productCode);

    List<Organizer> findByName(String name);

    Optional<Organizer> findByQrCode(String qrCode);

    boolean existsByQrCode(String qrCode);

}
