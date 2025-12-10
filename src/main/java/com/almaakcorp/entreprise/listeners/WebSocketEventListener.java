package com.almaakcorp.entreprise.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    // Store session to user mapping
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        // Extract user ID from headers if available
        String userIdHeader = headerAccessor.getFirstNativeHeader("userId");
        if (userIdHeader != null) {
            try {
                Long userId = Long.parseLong(userIdHeader);
                sessionUserMap.put(sessionId, userId);
                
                log.info("User {} connected via WebSocket with session {}", userId, sessionId);
            } catch (NumberFormatException e) {
                log.warn("Invalid userId in WebSocket header: {}", userIdHeader);
            }
        }
        
        log.debug("WebSocket connection established for session: {}", sessionId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        // Get user ID from session mapping
        Long userId = sessionUserMap.remove(sessionId);
        if (userId != null) {
            log.info("User {} disconnected from WebSocket, session {}", userId, sessionId);
        }
        
        log.debug("WebSocket connection closed for session: {}", sessionId);
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        
        log.debug("WebSocket subscription to {} for session: {}", destination, sessionId);
    }
    
    /**
     * Get currently connected users count
     */
    public int getConnectedUsersCount() {
        return sessionUserMap.size();
    }
    
    /**
     * Check if user is connected via WebSocket
     */
    public boolean isUserConnected(Long userId) {
        return sessionUserMap.containsValue(userId);
    }
    
    /**
     * Get all connected user IDs
     */
    public java.util.Set<Long> getConnectedUserIds() {
        return new java.util.HashSet<>(sessionUserMap.values());
    }
}