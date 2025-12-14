package com.usps.portal.repository;

import com.usps.portal.model.Shipment;
import com.usps.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    
    List<Shipment> findByUser(User user);
    
    @Query("SELECT s FROM Shipment s WHERE s.user = :user AND s.createdAt BETWEEN :startDate AND :endDate")
    List<Shipment> findByUserAndDateRange(
        @Param("user") User user,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}


