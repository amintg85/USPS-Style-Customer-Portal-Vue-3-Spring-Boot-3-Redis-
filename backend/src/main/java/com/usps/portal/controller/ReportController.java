package com.usps.portal.controller;

import com.usps.portal.model.User;
import com.usps.portal.service.ReportService;
import com.usps.portal.service.UserService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;
    private final com.usps.portal.config.RateLimitConfig.RateLimitService rateLimitService;

    @GetMapping("/shipment-report")
    public ResponseEntity<?> getShipmentReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpServletRequest request) {
        
        // Rate limiting
        String clientIp = getClientIp(request);
        Bucket bucket = rateLimitService.resolveBucket(clientIp);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("error", "Rate limit exceeded. Please try again later."));
        }

        User currentUser = getCurrentUser();
        Map<String, Object> report = reportService.generateShipmentReport(currentUser, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics(HttpServletRequest request) {
        // Rate limiting
        String clientIp = getClientIp(request);
        Bucket bucket = rateLimitService.resolveBucket(clientIp);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("error", "Rate limit exceeded. Please try again later."));
        }

        User currentUser = getCurrentUser();
        Map<String, Object> stats = reportService.generateUserStatistics(currentUser);
        return ResponseEntity.ok(stats);
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


