package com.almaakcorp.entreprise.audit;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

public class CurrentUser {

    public static String username() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return "anonymous";
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.User u) {
            return u.getUsername();
        }
        if (principal instanceof String s) {
            return s;
        }
        // JWT map-style principal
        if (principal instanceof Map<?,?> map) {
            Object sub = map.get("sub");
            if (sub != null) return String.valueOf(sub);
        }
        return String.valueOf(principal);
    }

    public static String userId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof Map<?,?> map) {
            Object id = map.get("id");
            if (id != null) return String.valueOf(id);
            Object sub = map.get("sub");
            if (sub != null) return String.valueOf(sub);
        }
        return null;
    }
}
