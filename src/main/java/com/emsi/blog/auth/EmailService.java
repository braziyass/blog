package com.emsi.blog.auth;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import lombok.RequiredArgsConstructor;

import com.emsi.blog.user.User;

import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.mail.from:no-reply@braziyas.dev}")
    private String mailFrom;

    @Value("${spring.mail.from.name:Brazi yassine}")
    private String mailFromName;

    @Value("${spring.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${spring.mail.password}")
    private String mailtrapToken;

    @Value("${spring.mail.api.url:https://send.api.mailtrap.io/api/send}")
    private String mailtrapApiUrl;

    private final WebClient.Builder webClientBuilder;

    /**
     * Send verification email. This method will NOT throw on Mail exceptions;
     * it logs and returns false so callers can continue (registration should not fail).
     */
    public boolean sendVerificationEmail(User user, String token) {
        if (!mailEnabled) {
            System.out.println("⚠️ Email disabled - skipping verification email for " + user.getEmail());
            System.out.println("📧 Verification token: " + token);
            System.out.println("🔗 Verify URL: https://blog-oawu.onrender.com/api/auth/verify?token=" + token);
            return true;
        }

        String to = user.getEmail();
        String subject = "Please verify your account";
        String verifyUrl = "https://blog-oawu.onrender.com/api/auth/verify?token=" + token;
        String htmlBody = String.format(
            "<h2>Hi %s,</h2>" +
            "<p>Please verify your account by clicking the button below:</p>" +
            "<a href='%s' style='display:inline-block;padding:10px 20px;background-color:#007bff;color:white;text-decoration:none;border-radius:5px;'>Verify Account</a>" +
            "<p>Or copy this link: <a href='%s'>%s</a></p>" +
            "<p>If you did not sign up, ignore this email.</p>" +
            "<p>Regards,<br/>Blog Team</p>",
            user.getFirstName(), verifyUrl, verifyUrl, verifyUrl
        );

        Map<String, Object> emailRequest = Map.of(
            "from", Map.of(
                "email", mailFrom,
                "name", mailFromName
            ),
            "to", List.of(Map.of("email", to)),
            "subject", subject,
            "html", htmlBody
        );

        try {
            WebClient webClient = webClientBuilder
                .baseUrl(mailtrapApiUrl)
                .defaultHeader("Authorization", "Bearer " + mailtrapToken)
                .defaultHeader("Content-Type", "application/json")
                .build();

            Mono<String> response = webClient.post()
                .bodyValue(emailRequest)
                .retrieve()
                .bodyToMono(String.class);

            response.block(); // blocking call for simplicity
            System.out.println("✅ Verification email sent via Mailtrap API to " + to);
            return true;
        } catch (Exception ex) {
            System.err.println("❌ Error sending verification email to " + to + ": " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
}
