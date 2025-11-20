package com.emsi.blog.config;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationProvider;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor

public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF
            .csrf(csrf -> csrf.disable())
            
            // Configure session management
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/blogs/**").permitAll() // allow public reads
                .requestMatchers("/api/auth/**").permitAll() // auth endpoints public
                 .anyRequest().authenticated())
            
            // Ensure unauthenticated requests are handled by our entry point (returns 401/redirect)
            .exceptionHandling(ex -> ex.authenticationEntryPoint(new RestAuthenticationEntryPoint()))
            
            // Add custom authentication provider and filter
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Custom entry point to handle unauthenticated requests (redirect browsers, return JSON for API clients)
	public static class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
		private static final String AUTH_URL = "/api/auth/authenticate";

		@Override
		public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
			String accept = request.getHeader("Accept");
			response.setHeader("Location", AUTH_URL);

			if (accept != null && accept.contains("text/html")) {
				// Browser -> redirect
				response.setStatus(HttpServletResponse.SC_FOUND); // 302
				response.sendRedirect(AUTH_URL);
				return;
			}

			// API client (Postman) -> return 401 JSON with authUrl
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			String body = String.format("{\"error\":\"Unauthenticated\",\"message\":\"%s\",\"authUrl\":\"%s\"}",
					authException == null ? "Authentication required" : authException.getMessage(),
					AUTH_URL);
			try (PrintWriter out = response.getWriter()) {
				out.print(body);
			}
		}
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:8081")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
