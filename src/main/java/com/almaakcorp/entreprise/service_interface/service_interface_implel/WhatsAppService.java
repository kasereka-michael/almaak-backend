package com.almaakcorp.entreprise.service_interface.service_interface_implel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class WhatsAppService {

    @Value("${whatsapp.green-api.instance-id:}")
    private String instanceId;

    @Value("${whatsapp.green-api.api-token:}")
    private String apiToken;

    private final WebClient webClient;

    public WhatsAppService() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
    }

    /**
     * Send a PDF document via WhatsApp using Green API
     *
     * @param phoneNumber The recipient's phone number (with country code, e.g., "1234567890")
     * @param pdfBytes The PDF file as byte array
     * @param fileName The name of the PDF file
     * @param caption Optional caption for the document
     * @return Success/failure message
     */
    public Mono<String> sendPdfDocument(String phoneNumber, byte[] pdfBytes, String fileName, String caption) {
        if (instanceId.isEmpty() || apiToken.isEmpty()) {
            return Mono.error(new RuntimeException("WhatsApp Green API credentials not configured"));
        }

        // Format phone number (ensure it has country code)
        String formattedNumber = formatPhoneNumber(phoneNumber);
        
        // Green API endpoint for sending documents
        String url = String.format("https://api.green-api.com/waInstance%s/sendFileByUpload/%s", 
                instanceId, apiToken);

        // Prepare multipart form data
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("chatId", formattedNumber + "@c.us");
        builder.part("file", new ByteArrayResource(pdfBytes))
                .header("Content-Disposition", "form-data; name=\"file\"; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF);
        
        if (caption != null && !caption.trim().isEmpty()) {
            builder.part("caption", caption);
        }

        return webClient.post()
                .uri(url)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.info("WhatsApp document sent successfully: {}", response))
                .doOnError(error -> log.error("Failed to send WhatsApp document: {}", error.getMessage()))
                .map(response -> "Document sent successfully via WhatsApp")
                .onErrorReturn("Failed to send document via WhatsApp");
    }

    /**
     * Send a text message via WhatsApp using Green API
     *
     * @param phoneNumber The recipient's phone number
     * @param message The text message to send
     * @return Success/failure message
     */
    public Mono<String> sendTextMessage(String phoneNumber, String message) {
        if (instanceId.isEmpty() || apiToken.isEmpty()) {
            return Mono.error(new RuntimeException("WhatsApp Green API credentials not configured"));
        }

        String formattedNumber = formatPhoneNumber(phoneNumber);
        String url = String.format("https://api.green-api.com/waInstance%s/sendMessage/%s", 
                instanceId, apiToken);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("chatId", formattedNumber + "@c.us");
        requestBody.put("message", message);

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.info("WhatsApp message sent successfully: {}", response))
                .doOnError(error -> log.error("Failed to send WhatsApp message: {}", error.getMessage()))
                .map(response -> "Message sent successfully via WhatsApp")
                .onErrorReturn("Failed to send message via WhatsApp");
    }

    /**
     * Format phone number to ensure it has proper format for WhatsApp
     * Removes any non-digit characters and ensures country code is present
     */
    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Phone number cannot be null");
        }
        
        // Remove all non-digit characters
        String cleaned = phoneNumber.replaceAll("[^\\d]", "");
        
        // If number doesn't start with country code, you might want to add default country code
        // For now, we'll assume the number is properly formatted with country code
        if (cleaned.length() < 10) {
            throw new IllegalArgumentException("Phone number is too short: " + phoneNumber);
        }
        
        return cleaned;
    }

    /**
     * Check if WhatsApp service is properly configured
     */
    public boolean isConfigured() {
        return !instanceId.isEmpty() && !apiToken.isEmpty();
    }
}