package com.college.service;

import com.college.dto.PageResponse;
import com.college.dto.VenueDTO;
import com.college.entity.Venue;
import com.college.exception.ResourceNotFoundException;
import com.college.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing venues.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class VenueService {

    private final VenueRepository venueRepository;

    /**
     * Get all active venues with pagination.
     */
    public PageResponse<VenueDTO> getAllVenues(int page, int size, String sort) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sort) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        Page<Venue> venues = venueRepository.findByActive(true, pageable);
        return convertToPageResponse(venues);
    }

    /**
     * Search venues by name.
     */
    public PageResponse<VenueDTO> searchVenues(String name, int page, int size, String sort) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sort) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "name"));
        Page<Venue> venues = venueRepository.findByNameContainingIgnoreCaseAndActive(name, true, pageable);
        return convertToPageResponse(venues);
    }

    /**
     * Get venue by ID.
     */
    @Transactional(readOnly = true)
    public VenueDTO getVenueById(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));
        return convertToDTO(venue);
    }

    /**
     * Create a new venue (admin only).
     */
    public VenueDTO createVenue(VenueDTO venueDTO) {
        Venue venue = new Venue();
        venue.setName(venueDTO.getName());
        venue.setDescription(venueDTO.getDescription());
        venue.setAddress(venueDTO.getAddress());
        venue.setCapacity(venueDTO.getCapacity());
        venue.setCity(venueDTO.getCity());
        venue.setState(venueDTO.getState());
        venue.setZipCode(venueDTO.getZipCode());
        venue.setLatitude(venueDTO.getLatitude());
        venue.setLongitude(venueDTO.getLongitude());
        venue.setContactPhone(venueDTO.getContactPhone());
        venue.setContactEmail(venueDTO.getContactEmail());
        venue.setActive(true);

        Venue savedVenue = venueRepository.save(venue);
        log.info("Venue created with id: {}", savedVenue.getId());
        return convertToDTO(savedVenue);
    }

    /**
     * Update an existing venue (admin only).
     */
    public VenueDTO updateVenue(Long id, VenueDTO venueDTO) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));

        if (venueDTO.getName() != null) venue.setName(venueDTO.getName());
        if (venueDTO.getDescription() != null) venue.setDescription(venueDTO.getDescription());
        if (venueDTO.getAddress() != null) venue.setAddress(venueDTO.getAddress());
        if (venueDTO.getCapacity() != null) venue.setCapacity(venueDTO.getCapacity());
        if (venueDTO.getCity() != null) venue.setCity(venueDTO.getCity());
        if (venueDTO.getState() != null) venue.setState(venueDTO.getState());
        if (venueDTO.getZipCode() != null) venue.setZipCode(venueDTO.getZipCode());
        if (venueDTO.getLatitude() != null) venue.setLatitude(venueDTO.getLatitude());
        if (venueDTO.getLongitude() != null) venue.setLongitude(venueDTO.getLongitude());
        if (venueDTO.getContactPhone() != null) venue.setContactPhone(venueDTO.getContactPhone());
        if (venueDTO.getContactEmail() != null) venue.setContactEmail(venueDTO.getContactEmail());

        Venue updated = venueRepository.save(venue);
        log.info("Venue updated with id: {}", id);
        return convertToDTO(updated);
    }

    /**
     * Delete a venue (admin only).
     */
    public void deleteVenue(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));
        venue.setActive(false);
        venueRepository.save(venue);
        log.info("Venue deleted with id: {}", id);
    }

    private VenueDTO convertToDTO(Venue venue) {
        return VenueDTO.builder()
                .id(venue.getId())
                .name(venue.getName())
                .description(venue.getDescription())
                .address(venue.getAddress())
                .capacity(venue.getCapacity())
                .city(venue.getCity())
                .state(venue.getState())
                .zipCode(venue.getZipCode())
                .latitude(venue.getLatitude())
                .longitude(venue.getLongitude())
                .contactPhone(venue.getContactPhone())
                .contactEmail(venue.getContactEmail())
                .active(venue.getActive())
                .createdAt(venue.getCreatedAt())
                .updatedAt(venue.getUpdatedAt())
                .build();
    }

    private PageResponse<VenueDTO> convertToPageResponse(Page<Venue> venues) {
        return PageResponse.<VenueDTO>builder()
                .content(venues.getContent().stream().map(this::convertToDTO).toList())
                .totalElements(venues.getTotalElements())
                .totalPages(venues.getTotalPages())
                .pageNumber(venues.getNumber())
                .pageSize(venues.getSize())
                .last(venues.isLast())
                .build();
    }
}
