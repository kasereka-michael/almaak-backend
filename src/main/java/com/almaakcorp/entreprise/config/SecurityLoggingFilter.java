package com.almaakcorp.entreprise.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SecurityLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        // Only log API requests (not static resources)
        if (requestURI.startsWith("/api/")) {
            logger.info("🚀 Backend Request: {} {}", method, requestURI);
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null) {
                logger.info("👤 Authentication present: {}", authentication.getClass().getSimpleName());
                logger.info("📧 Principal: {}", authentication.getName());
                logger.info("🔐 Is authenticated: {}", Optional.of(authentication.isAuthenticated()));
                
                if (authentication.getAuthorities() != null) {
                    String authorities = authentication.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.joining(", "));
                    logger.info("🎭 Authorities: [{}]", authorities);
                } else {
                    logger.warn("⚠️ No authorities found for user");
                }
            } else {
                logger.warn("❌ No authentication found in SecurityContext");
            }
            
            // Log session info
            if (request.getSession(false) != null) {
                logger.info("🍪 Session ID: {}", request.getSession().getId());
            } else {
                logger.warn("❌ No session found");
            }
        }

        filterChain.doFilter(request, response);
        
        // Log response status for API requests
        if (requestURI.startsWith("/api/")) {
            logger.info("📤 Response: {} {} -> {}", (Object) method, (Object) requestURI, (Object) response.getStatus());
        }
    }
}