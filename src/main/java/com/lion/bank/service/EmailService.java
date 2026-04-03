package com.lion.bank.service;

import com.resend.Resend;
import com.resend.services.emails.model.SendEmailResponse;
import com.resend.services.emails.model.SendEmailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final Resend resend;

    public EmailService(@Value("${resend.api.key}") String apiKey) {
        this.resend = new Resend(apiKey);
    }

    public void sendVerificationEmail(String to, String token) {
        SendEmailRequest params = SendEmailRequest.builder()
                .from("Lion Bank <onboarding@resend.dev>")
                .to(to)
                .subject("Verifica tu cuenta - Lion Bank")
                .html("<h1>Bienvenido a Lion Bank</h1>"
                        + "<p>Gracias por registrarte.</p>"
                        + "<p>Tu código de verificación es: <strong>" + token + "</strong></p>"
                        + "<p>Si no creaste esta cuenta, ignora este correo.</p>")
                .build();

        try {
            SendEmailResponse response = resend.emails().send(params);
            System.out.println("Email sent: " + response.getId());
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            throw new RuntimeException("Error al enviar el correo de verificación", e);
        }
    }
}
