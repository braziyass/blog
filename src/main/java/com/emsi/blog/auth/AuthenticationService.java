package com.emsi.blog.auth;

import com.emsi.blog.config.JwtService;
import com.emsi.blog.user.*;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService; // new

    public AuthenticationResponse register(RegisterRequest request) {
        // if email already exists, return informative response (do not throw / do not try to save)
        if (repository.findByEmail(request.getEmail()).isPresent()) {
            return AuthenticationResponse.builder()
                    .token(null)
                    .message("Email already registered")
                    .build();
        }

        User user = User.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(Role.USER)
            .verified(false) // ensure not enabled until verification
            .build();

        // generate verification token and save
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        repository.save(user);

        // send verification email - do not let failures abort registration
        boolean emailOk = emailService.sendVerificationEmail(user, verificationToken);
        if (!emailOk) {
            return AuthenticationResponse.builder()
                .token(null)
                .message("Account created, but verification email failed to send. Check logs or contact admin.")
                .build();
        }

        return AuthenticationResponse.builder()
            .token(null)
            .message("Verification email sent. Please check your inbox.")
            .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // load user first to check verification state
        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.isVerified()) {
            // return a response instructing the client to verify instead of throwing
            return AuthenticationResponse.builder()
                    .token(null)
                    .message("Account not verified. Please verify your account (check email) or call /api/auth/verify?token=<token>).")
                    .build();
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        String jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
            .token(jwtToken)
            .message("Authenticated")
            .build();
    }

    // changed: return AuthenticationResponse with JWT instead of boolean
    public AuthenticationResponse verifyAccount(String token) {
        User user = repository.findAll().stream()
                .filter(u -> token.equals(u.getVerificationToken()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        user.setVerified(true);
        user.setVerificationToken(null);
        repository.save(user);

        // generate JWT for the now-verified user
        String jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .message("Account verified. Use this token to authenticate requests.")
                .build();
    }

}
