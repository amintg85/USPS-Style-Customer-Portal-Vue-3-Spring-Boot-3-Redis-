package com.usps.portal.repository;

import com.usps.portal.model.Shipment;
import com.usps.portal.model.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {
    List<TrackingEvent> findByShipmentOrderByEventTimeDesc(Shipment shipment);
}


