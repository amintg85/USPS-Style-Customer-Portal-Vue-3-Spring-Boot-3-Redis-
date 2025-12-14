package com.usps.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableAsync
public class CustomerPortalApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerPortalApplication.class, args);
    }
}


