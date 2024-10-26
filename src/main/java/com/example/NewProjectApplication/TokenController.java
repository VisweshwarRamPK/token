package com.example.NewProjectApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
public class TokenController {

    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);

    @GetMapping("/getToken")
    public ResponseEntity<Map<String, Object>> getToken() {
        logger.info("Request received to get token");
        try {
            String url = "https://api-uat.mmfsl.com/oauth/cc/v1/token";

            // Set up Basic Authentication
            String username = "kA6JS6iNRGxiAAZBPwhYX56b1DJ3Eby3LHl236L68roNHyPa";
            String password = "AYGURivZadXP1YiJdO2xgqCBcaYKXGtZjq9Lba08SYygr0q0AK3enbSxPESWKwBS";
            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encodedAuth);
            headers.set("Content-Type", "application/x-www-form-urlencoded");

            // Create request body
            String body = "grant_type=client_credentials";
            HttpEntity<String> request = new HttpEntity<>(body, headers);

            // Make the API call
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            // Log the response status
            logger.info("Response Status Code: {}", response.getStatusCode());

            // Check response status and return the result
            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("Token retrieved successfully");
                return ResponseEntity.ok(response.getBody());
            } else {
                logger.error("Error retrieving token: {}", response.getStatusCode());
                return ResponseEntity.status(response.getStatusCode()).body(Map.of("error", "Error: " + response.getStatusCode()));
            }
        } catch (Exception e) {
            logger.error("Internal server error occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal Server Error: " + e.getMessage()));
        }
    }
}
