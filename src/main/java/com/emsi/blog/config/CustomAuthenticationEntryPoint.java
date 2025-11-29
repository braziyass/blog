package com.emsi.blog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String AUTH_URL = "/api/auth/authenticate";
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
                        AuthenticationException authException) throws IOException {
        
        String acceptHeader = request.getHeader("Accept");
        response.setHeader("Location", AUTH_URL);

        if (isBrowserRequest(acceptHeader)) {
            handleBrowserRequest(response);
        } else {
            handleApiRequest(response, authException);
        }
    }

    private boolean isBrowserRequest(String acceptHeader) {
        return acceptHeader != null && acceptHeader.contains("text/html");
    }

    private void handleBrowserRequest(HttpServletResponse response) throws IOException {
        log.debug("Browser request detected, redirecting to: {}", AUTH_URL);
        response.setStatus(HttpServletResponse.SC_FOUND);
        response.sendRedirect(AUTH_URL);
    }

    private void handleApiRequest(HttpServletResponse response, 
                                  AuthenticationException authException) throws IOException {
        log.debug("API request detected, returning 401 JSON response");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Unauthenticated");
        errorResponse.put("message", getErrorMessage(authException));
        errorResponse.put("authUrl", AUTH_URL);

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    private String getErrorMessage(AuthenticationException authException) {
        return authException != null ? authException.getMessage() : "Authentication required";
    }
}
