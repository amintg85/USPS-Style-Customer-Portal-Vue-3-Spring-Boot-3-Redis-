package com.usps.portal.service;

import com.usps.portal.model.Shipment;
import com.usps.portal.model.TrackingEvent;
import com.usps.portal.model.User;
import com.usps.portal.repository.ShipmentRepository;
import com.usps.portal.repository.TrackingEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final TrackingEventRepository trackingEventRepository;

    @Transactional(readOnly = true)
    public Shipment findByTrackingNumber(String trackingNumber) {
        Optional<Shipment> shipmentOpt = shipmentRepository.findByTrackingNumber(trackingNumber);
        if (shipmentOpt.isEmpty()) {
            throw new RuntimeException("Shipment not found: " + trackingNumber);
        }
        Shipment shipment = shipmentOpt.get();
        // Eagerly fetch user to avoid lazy loading issues
        shipment.getUser().getId();
        return shipment;
    }

    @Cacheable(value = "userShipments", key = "#user.id")
    @Transactional(readOnly = true)
    public List<Shipment> findByUser(User user) {
        List<Shipment> shipments = shipmentRepository.findByUser(user);
        // Eagerly fetch user for each shipment to avoid lazy loading issues
        shipments.forEach(s -> s.getUser().getId());
        return shipments;
    }

    @Transactional
    @CacheEvict(value = "userShipments", key = "#user.id", allEntries = false)
    public Shipment createShipment(User user, Shipment shipment) {
        shipment.setUser(user);
        String trackingNumber = generateTrackingNumber();
        shipment.setTrackingNumber(trackingNumber);
        shipment.setStatus(Shipment.ShipmentStatus.PENDING);
        shipment.setCreatedAt(LocalDateTime.now());
        shipment.setUpdatedAt(LocalDateTime.now());
        
        Shipment saved = shipmentRepository.saveAndFlush(shipment);
        
        // Create initial tracking event
        TrackingEvent initialEvent = TrackingEvent.builder()
            .shipment(saved)
            .location("Origin Facility")
            .description("Shipment created and pending pickup")
            .eventTime(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .build();
        trackingEventRepository.saveAndFlush(initialEvent);
        
        return saved;
    }

    @Transactional
    public Shipment updateShipmentStatus(String trackingNumber, Shipment.ShipmentStatus status) {
        Shipment shipment = findByTrackingNumber(trackingNumber);
        shipment.setStatus(status);
        shipment.setUpdatedAt(LocalDateTime.now());
        
        if (status == Shipment.ShipmentStatus.DELIVERED) {
            shipment.setDeliveredAt(LocalDateTime.now());
        }
        
        return shipmentRepository.save(shipment);
    }

    @Cacheable(value = "trackingEvents", key = "#trackingNumber")
    public List<TrackingEvent> getTrackingEvents(String trackingNumber) {
        Shipment shipment = findByTrackingNumber(trackingNumber);
        return trackingEventRepository.findByShipmentOrderByEventTimeDesc(shipment);
    }

    private String generateTrackingNumber() {
        return "USPS" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}


