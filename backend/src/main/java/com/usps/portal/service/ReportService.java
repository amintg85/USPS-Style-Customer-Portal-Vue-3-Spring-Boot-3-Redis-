package com.usps.portal.service;

import com.usps.portal.model.Shipment;
import com.usps.portal.model.User;
import com.usps.portal.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ShipmentRepository shipmentRepository;

    @Cacheable(value = "shipmentReports", key = "#user.id + '_' + #startDate + '_' + #endDate")
    public Map<String, Object> generateShipmentReport(User user, LocalDateTime startDate, LocalDateTime endDate) {
        List<Shipment> shipments = shipmentRepository.findByUserAndDateRange(user, startDate, endDate);
        
        Map<String, Object> report = new HashMap<>();
        report.put("totalShipments", shipments.size());
        report.put("period", Map.of("start", startDate, "end", endDate));
        
        Map<String, Long> statusCounts = new HashMap<>();
        for (Shipment.ShipmentStatus status : Shipment.ShipmentStatus.values()) {
            statusCounts.put(status.name(), shipments.stream()
                .filter(s -> s.getStatus() == status)
                .count());
        }
        report.put("statusCounts", statusCounts);
        
        long deliveredCount = statusCounts.getOrDefault("DELIVERED", 0L);
        report.put("deliveryRate", shipments.isEmpty() ? 0.0 : 
            (double) deliveredCount / shipments.size() * 100);
        
        report.put("shipments", shipments);
        
        return report;
    }

    public Map<String, Object> generateUserStatistics(User user) {
        List<Shipment> allShipments = shipmentRepository.findByUser(user);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalShipments", allShipments.size());
        
        Map<String, Long> statusCounts = new HashMap<>();
        for (Shipment.ShipmentStatus status : Shipment.ShipmentStatus.values()) {
            statusCounts.put(status.name(), allShipments.stream()
                .filter(s -> s.getStatus() == status)
                .count());
        }
        stats.put("statusCounts", statusCounts);
        
        return stats;
    }
}


