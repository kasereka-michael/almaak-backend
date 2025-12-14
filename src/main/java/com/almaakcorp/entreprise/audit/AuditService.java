package com.almaakcorp.entreprise.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void log(String action, String entity, String entityId, Object details, boolean success, String error) {
        HttpServletRequest request = currentRequest();
        String path = request != null ? request.getRequestURI() : null;
        String method = request != null ? request.getMethod() : null;
        String ip = request != null ? request.getRemoteAddr() : null;

        AuditLog log = AuditLog.builder()
                .action(action)
                .entity(entity)
                .entityId(entityId)
                .username(CurrentUser.username())
                .userId(CurrentUser.userId())
                .path(path)
                .httpMethod(method)
                .ip(ip)
                .details(safeJson(details))
                .success(success)
                .error(error)
                .build();
        repository.save(log);
    }

    private String safeJson(Object details) {
        if (details == null) return null;
        try {
            String json;
            if (details instanceof String s) {
                json = s;
            } else if (details instanceof Map) {
                json = objectMapper.writeValueAsString(details);
            } else {
                Map<String, Object> wrapper = new HashMap<>();
                wrapper.put("data", details);
                json = objectMapper.writeValueAsString(wrapper);
            }
            // truncate to ~4000 to protect row size
            return json.length() > 4000 ? json.substring(0, 4000) : json;
        } catch (JsonProcessingException e) {
            return String.valueOf(details);
        }
    }

    private HttpServletRequest currentRequest() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs != null ? attrs.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
