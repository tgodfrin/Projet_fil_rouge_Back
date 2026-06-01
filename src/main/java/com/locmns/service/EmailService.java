package com.locmns.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    /**
     * Sends a temporary password to the user by email.
     * The password is sent in plain text — it will be changed on first login.
     */
    public void sendPasswordEmail(String toEmail, String name, String temporaryPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Loc-MNS — Votre mot de passe");
            message.setText(
                "Bonjour " + name + ",\n\n" +
                "Un nouveau mot de passe a été généré pour votre compte Loc-MNS :\n\n" +
                "    " + temporaryPassword + "\n\n" +
                "Connectez-vous et changez ce mot de passe dès que possible.\n\n" +
                "L'équipe Loc-MNS"
            );
            mailSender.send(message);
        } catch (Exception e) {
            // Log the error but do not propagate — password is already saved in DB
            log.error("Échec de l'envoi de l'email à {} : {}", toEmail, e.getMessage());
        }
    }
}
