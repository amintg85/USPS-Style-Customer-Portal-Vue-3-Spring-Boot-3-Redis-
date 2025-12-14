package com.usps.portal.controller;

import com.usps.portal.model.Shipment;
import com.usps.portal.model.TrackingEvent;
import com.usps.portal.model.User;
import com.usps.portal.service.ShipmentService;
import com.usps.portal.service.UserService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final ShipmentService shipmentService;
    private final UserService userService;
    private final com.usps.portal.config.RateLimitConfig.RateLimitService rateLimitService;

    @GetMapping("/{trackingNumber}")
    public ResponseEntity<?> trackShipment(
            @PathVariable String trackingNumber,
            HttpServletRequest request) {
        
        // Rate limiting
        String clientIp = getClientIp(request);
        Bucket bucket = rateLimitService.resolveBucket(clientIp);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("error", "Rate limit exceeded. Please try again later."));
        }

        try {
            Shipment shipment = shipmentService.findByTrackingNumber(trackingNumber);
            User currentUser = getCurrentUser();
            
            // Verify ownership - access user ID to trigger lazy loading within transaction
            Long shipmentUserId = shipment.getUser().getId();
            if (!shipmentUserId.equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied"));
            }
            
            List<TrackingEvent> events = shipmentService.getTrackingEvents(trackingNumber);
            
            return ResponseEntity.ok(Map.of(
                "shipment", shipment,
                "events", events
            ));
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Shipment not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Shipment not found"));
            }
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Shipment not found: " + e.getMessage()));
        }
    }

    @GetMapping("/my-shipments")
    public ResponseEntity<?> getMyShipments(HttpServletRequest request) {
        // Rate limiting
        String clientIp = getClientIp(request);
        Bucket bucket = rateLimitService.resolveBucket(clientIp);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("error", "Rate limit exceeded. Please try again later."));
        }

        User currentUser = getCurrentUser();
        List<Shipment> shipments = shipmentService.findByUser(currentUser);
        return ResponseEntity.ok(Map.of("shipments", shipments));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createShipment(
            @RequestBody com.usps.portal.dto.ShipmentRequest request,
            HttpServletRequest httpRequest) {
        
        // Rate limiting
        String clientIp = getClientIp(httpRequest);
        Bucket bucket = rateLimitService.resolveBucket(clientIp);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("error", "Rate limit exceeded. Please try again later."));
        }

        User currentUser = getCurrentUser();
        
        Shipment shipment = Shipment.builder()
            .recipientName(request.getRecipientName())
            .recipientAddress(request.getRecipientAddress())
            .recipientCity(request.getRecipientCity())
            .recipientState(request.getRecipientState())
            .recipientZipCode(request.getRecipientZipCode())
            .build();
        
        Shipment created = shipmentService.createShipment(currentUser, shipment);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userService.findByEmail(email);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}


