package com.usps.portal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentRequest {
    @NotBlank(message = "Recipient name is required")
    private String recipientName;
    
    @NotBlank(message = "Recipient address is required")
    private String recipientAddress;
    
    @NotBlank(message = "Recipient city is required")
    private String recipientCity;
    
    @NotBlank(message = "Recipient state is required")
    private String recipientState;
    
    @NotBlank(message = "Recipient zip code is required")
    private String recipientZipCode;
}


