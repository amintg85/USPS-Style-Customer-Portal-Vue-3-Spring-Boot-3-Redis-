package com.usps.portal.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String trackingNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User user;
    
    @Column(nullable = false)
    private String recipientName;
    
    @Column(nullable = false)
    private String recipientAddress;
    
    @Column(nullable = false)
    private String recipientCity;
    
    @Column(nullable = false)
    private String recipientState;
    
    @Column(nullable = false)
    private String recipientZipCode;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    private LocalDateTime deliveredAt;
    
    public enum ShipmentStatus {
        PENDING, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, EXCEPTION
    }
}


