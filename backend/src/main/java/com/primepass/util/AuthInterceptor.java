package com.primepass.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Allow OPTIONS requests (CORS preflight)
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // Public endpoints that don't need authentication
        String path = request.getRequestURI();
        if (path.startsWith("/auth/") || path.equals("/events") || path.startsWith("/events/") 
            || path.startsWith("/seats") || path.startsWith("/showtimes/")) {
            return true;
        }

        // Check for token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // Store user info in request
        request.setAttribute("userId", jwtUtil.getUserIdFromToken(token));
        request.setAttribute("role", jwtUtil.getRoleFromToken(token));
        return true;
    }
}

