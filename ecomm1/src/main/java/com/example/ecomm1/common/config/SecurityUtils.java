package com.example.ecomm1.common.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        throw new IllegalStateException("User not authenticated");
    }

    public static String getCurrentRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            throw new IllegalStateException("User not authenticated");
        }

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority == null || authority.getAuthority() == null) continue;
            String a = authority.getAuthority();
            if (a.startsWith("ROLE_")) {
                return a.substring("ROLE_".length());
            }
            return a;
        }

        throw new IllegalStateException("User role not available");
    }

    public static void requireRole(String role) {
        String currentRole = getCurrentRole();
        if (!role.equals(currentRole)) {
            throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        }
    }
}
