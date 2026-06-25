package com.enterprise.poodle.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;

@Service
public class PDFReaderService {

    private final RestTemplate restTemplate;

    public PDFReaderService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public byte[] getPdfContent(String url) {
        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, null, byte[].class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                // You can log the error status code here
                return null;
            }
        } catch (Exception e) {
            // You can log the exception here
            return null;
        }
    }
}
