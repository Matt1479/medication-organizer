package com.matt1479.medicationorganizer.organizers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.matt1479.medicationorganizer.organizers.dto.CompartmentRequest;
import com.matt1479.medicationorganizer.organizers.dto.CreateOrganizerRequest;
import com.matt1479.medicationorganizer.organizers.dto.OrganizerResponse;
import com.matt1479.medicationorganizer.organizers.dto.UpdateDescriptionRequest;
import com.matt1479.medicationorganizer.organizers.dto.UpdateQrCodeRequest;
import com.matt1479.medicationorganizer.organizers.dto.UpdateValueRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/organizers")
@Validated
public class OrganizerController {

    private final OrganizerService organizerService;

    public OrganizerController(OrganizerService organizerService) {
        this.organizerService = organizerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizerResponse createOrganizer(@RequestBody @Valid CreateOrganizerRequest request) {
        List<Compartment> compartments = request.compartments().stream()
                .map((c) -> new Compartment(
                        c.dayOfWeek(),
                        c.timeOfDay(),
                        c.timeFrom(),
                        c.timeTo()))
                .toList();

        return organizerService.createOrganizer(
                request.productCode(),
                request.name(),
                request.daysCovered(),
                request.compartmentsPerDay(),
                request.description(),
                request.qrCode(),
                compartments);
    }

    @GetMapping("/{id}")
    public OrganizerResponse getOrganizerById(@PathVariable @Min(1) Long id) {
        return organizerService.getOrganizerById(id);
    }

    @GetMapping(params = { "productCode" })
    public OrganizerResponse getOrganizerByProductCode(@RequestParam @NotBlank String productCode) {
        return organizerService.getOrganizerByProductCode(productCode);
    }

    @GetMapping(params = { "name" })
    public List<OrganizerResponse> getOrganizersByName(@RequestParam @NotBlank String name) {
        return organizerService.getOrganizersByName(name);
    }

    @GetMapping(params = { "qrCode" })
    public OrganizerResponse getOrganizerByQrCode(@RequestParam @NotBlank String qrCode) {
        return organizerService.getOrganizerByQrCode(qrCode);
    }

    @GetMapping(params = { "!productCode", "!name", "!qrCode" })
    public List<OrganizerResponse> getAllOrganizers() {
        return organizerService.getAllOrganizers();
    }

    @PatchMapping("/{id}/product-code")
    public OrganizerResponse changeProductCode(
            @PathVariable @Min(1) Long id,
            @RequestBody @Valid UpdateValueRequest request) {

        return organizerService.changeProductCode(id, request.value());
    }

    @PatchMapping("/{id}/name")
    public OrganizerResponse changeName(
            @PathVariable @Min(1) Long id,
            @RequestBody @Valid UpdateValueRequest request) {

        return organizerService.changeName(id, request.value());
    }

    @PatchMapping("/{id}/description")
    public OrganizerResponse changeDescription(
            @PathVariable @Min(1) Long id,
            @RequestBody @Valid UpdateDescriptionRequest request) {

        return organizerService.changeDescription(id, request.description());
    }

    @PatchMapping("/{id}/qr-code")
    public OrganizerResponse changeQrCode(
            @PathVariable @Min(1) Long id,
            @RequestBody @Valid UpdateQrCodeRequest request) {

        return organizerService.changeQrCode(id, request.qrCode());
    }

    @PatchMapping("/{id}/activate")
    public OrganizerResponse activateOrganizer(@PathVariable @Min(1) Long id) {
        return organizerService.activateOrganizer(id);
    }

    @PatchMapping("/{id}/deactivate")
    public OrganizerResponse deactivateOrganizer(@PathVariable @Min(1) Long id) {
        return organizerService.deactivateOrganizer(id);
    }

    @PutMapping("/{organizerId}/compartments/{compartmentId}")
    public OrganizerResponse updateCompartment(
            @PathVariable @Min(1) Long organizerId,
            @PathVariable @Min(1) Long compartmentId,
            @RequestBody @Valid CompartmentRequest request) {

        return organizerService.updateCompartment(
                organizerId,
                compartmentId,
                request.dayOfWeek(),
                request.timeOfDay(),
                request.timeFrom(),
                request.timeTo());
    }

}
