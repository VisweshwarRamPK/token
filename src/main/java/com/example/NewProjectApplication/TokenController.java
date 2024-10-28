package com.example.NewProjectApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TokenController {

    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);

    @GetMapping("/getToken")
    public ResponseEntity<Map<String, Object>> getToken(@RequestBody Map<String, String> requestBody) {
        logger.info("Request received to get token and access external API");
        String token;

        try {
            
            String tokenUrl = "https://api-uat.mmfsl.com/oauth/cc/v1/token";
            String username = "kA6JS6iNRGxiAAZBPwhYX56b1DJ3Eby3LHl236L68roNHyPa";
            String password = "AYGURivZadXP1YiJdO2xgqCBcaYKXGtZjq9Lba08SYygr0q0AK3enbSxPESWKwBS";
            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encodedAuth);
            headers.set("Content-Type", "application/x-www-form-urlencoded");

           
            String body = "grant_type=client_credentials";
            HttpEntity<String> request = new HttpEntity<>(body, headers);

           
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> tokenResponse = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, Map.class);

            if (tokenResponse.getStatusCode() == HttpStatus.OK) {
                token = (String) tokenResponse.getBody().get("access_token");
                logger.info("Token retrieved successfully: {}", token);
            } else {
                logger.error("Error retrieving token: {}", tokenResponse.getStatusCode());
                return ResponseEntity.status(tokenResponse.getStatusCode()).body(Map.of("error", "Error retrieving token"));
            }

           
            String apiUrl = "https://api-uat.mmfsl.com/services/v1.1/getPanDetails";
            HttpHeaders apiHeaders = new HttpHeaders();
            apiHeaders.set("Authorization", "Bearer " + token);
            apiHeaders.set("Content-Type", "application/json");

           
            Map<String, Object> panRequestBody = new HashMap<>();
            panRequestBody.put("User_Id", "");
            panRequestBody.put("Module_Code", "LP");
            panRequestBody.put("chkNSDL", "Y");

           
            Map<String, String> panDetail = new HashMap<>();
            panDetail.put("pan", requestBody.get("pan_card")); 
            panDetail.put("name", requestBody.get("name")); 
            panDetail.put("fathername", ""); 
            panDetail.put("dob", "14/01/2003"); 

            
            panRequestBody.put("PAN_Details", List.of(panDetail));

            
            HttpEntity<Map<String, Object>> apiRequest = new HttpEntity<>(panRequestBody, apiHeaders);
            ResponseEntity<Map> apiResponse = restTemplate.exchange(apiUrl, HttpMethod.POST, apiRequest, Map.class);

            
            if (apiResponse.getStatusCode() == HttpStatus.OK) {
                logger.info("External API response received successfully");
                Map<String, Object> responseBody = apiResponse.getBody();
                
              
                List<Map<String, Object>> panStatusDetails = (List<Map<String, Object>>) responseBody.get("PAN_Status_Details");
                
                
                if (panStatusDetails != null && !panStatusDetails.isEmpty()) {
                    String panStatusDesc = (String) panStatusDetails.get(0).get("pan_status_desc");

                   
                    if ("VALID".equalsIgnoreCase(panStatusDesc)) {
                        return ResponseEntity.ok(Map.of("pan_status_desc", "valid"));
                    } else {
                        return ResponseEntity.ok(Map.of("pan_status_desc", "invalid"));
                    }
                } else {
                    logger.warn("No PAN status details found in response");
                    return ResponseEntity.ok(Map.of("pan_status_desc", "unknown")); 
                }
            } else {
                logger.error("Error accessing external API: {}", apiResponse.getStatusCode());
                return ResponseEntity.status(apiResponse.getStatusCode()).body(Map.of("error", "Error accessing external API"));
            }

        } catch (Exception e) {
            logger.error("Internal server error occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal Server Error: " + e.getMessage()));
        }
    }
}
