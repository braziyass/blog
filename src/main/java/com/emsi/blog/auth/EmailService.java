package com.emsi.blog.auth;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.mail.MailException;
import org.springframework.beans.factory.annotation.Value;

import lombok.RequiredArgsConstructor;

import com.emsi.blog.user.User;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // configurable From address (fallback to Mailtrap example sender)
    @Value("${spring.mail.from:no-reply@braziyas.dev}")
    private String mailFrom;

    // optional display name for the From header (fallback)
    @Value("${spring.mail.from.name:Brazi yassine}")
    private String mailFromName;

    /**
     * Send verification email. This method will NOT throw on Mail exceptions;
     * it logs and returns false so callers can continue (registration should not fail).
     */
    public boolean sendVerificationEmail(User user, String token) {
        String to = user.getEmail();
        String subject = "Please verify your account";
        String verifyUrl = "http://localhost:8080/api/auth/verify?token=" + token;
        String body = "Hi " + user.getFirstName() + ",\n\n" +
                "Please verify your account by clicking the link below:\n" +
                verifyUrl + "\n\n" +
                "If you did not sign up, ignore this email.\n\n" +
                "Regards,\nBlog Team";

        SimpleMailMessage message = new SimpleMailMessage();
        // set a proper From header with display name and set Reply‑To
        String fromHeader = String.format("%s <%s>", mailFromName, mailFrom);
        message.setFrom(fromHeader);
        message.setReplyTo(mailFrom);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            System.out.println("Verification email queued/sent to " + to);
            return true;
        } catch (MailException mex) {
            // don't rethrow; log and let caller decide how to proceed
            System.err.println("MailException sending verification email to " + to + ": " + mex.getMessage());
            mex.printStackTrace();
            return false;
        } catch (Exception ex) {
            System.err.println("Unexpected error sending verification email to " + to + ": " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
}
