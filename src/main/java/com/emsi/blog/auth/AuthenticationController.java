package com.emsi.blog.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth") // changed from "/auth" to match SecurityConfiguration
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
        @RequestBody RegisterRequest request) {
            System.out.println("Received registration request: " + request);
            AuthenticationResponse resp = service.register(request);
            if (resp.getToken() == null) {
                String msg = resp.getMessage() == null ? "" : resp.getMessage().toLowerCase();
                if (msg.contains("already registered") || msg.contains("already")) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(resp);
                }
                // account created but no token (verification email sent or failed)
                return ResponseEntity.status(HttpStatus.CREATED).body(resp);
            }
            return ResponseEntity.ok(resp);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
        @RequestBody AuthenticationRequest request) {
            AuthenticationResponse resp = service.authenticate(request);
            if (resp.getToken() == null) {
                // return 401 so client receives the service message (e.g., "Wrong credentials")
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
            }
            return ResponseEntity.ok(resp);
    }

    @GetMapping("/verify")
    public ResponseEntity<AuthenticationResponse> verifyAccount(@RequestParam("token") String token) {
        AuthenticationResponse resp = service.verifyAccount(token);
        return ResponseEntity.ok(resp);
    }

}
