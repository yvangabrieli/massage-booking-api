package com.massage.booking.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class EmailNotificationService {

    @Value("${sendgrid.api-key:}")
    private String apiKey;

    @Value("${sendgrid.from-email:tokamemassage@gmail.com}")
    private String fromEmail;

    @Value("${sendgrid.from-name:Tokame Massage}")
    private String fromName;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'at' HH:mm");

    @Async
    public void sendWelcomeEmail(String to, String name) {
        String subject = "Welcome to Tokame Massage \uD83D\uDC86";
        String body =
                "Hello " + name + ",\n\n" +
                        "Welcome to Tokame Massage! Your account has been created successfully.\n\n" +
                        "You can now book your massage sessions online.\n\n" +
                        "We look forward to seeing you!\n\n" +
                        "The Tokame Team";
        send(to, subject, body);
    }

    @Async
    public void sendBookingConfirmation(String to, String name, String serviceName,
                                        LocalDateTime startTime) {
        String subject = "Booking Confirmed - Tokame Massage";
        String body =
                "Hello " + name + ",\n\n" +
                        "Your booking has been confirmed!\n\n" +
                        "Service: " + serviceName + "\n" +
                        "Date & Time: " + startTime.format(FORMATTER) + "\n\n" +
                        "Please remember:\n" +
                        "- You can cancel up to 12 hours before your appointment\n" +
                        "- Arrive 5 minutes early\n\n" +
                        "See you soon!\n\n" +
                        "The Tokame Team";
        send(to, subject, body);
    }

    @Async
    public void sendBookingCancellation(String to, String name, String serviceName,
                                        LocalDateTime startTime, String reason) {
        String subject = "Booking Cancelled - Tokame Massage";
        String body =
                "Hello " + name + ",\n\n" +
                        "Your booking has been cancelled.\n\n" +
                        "Service: " + serviceName + "\n" +
                        "Date & Time: " + startTime.format(FORMATTER) + "\n" +
                        (reason != null ? "Reason: " + reason + "\n" : "") + "\n" +
                        "Feel free to book a new appointment anytime.\n\n" +
                        "The Tokame Team";
        send(to, subject, body);
    }

    private void send(String to, String subject, String body) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("SendGrid API key not configured - skipping email to: {}", to);
            return;
        }

        try {
            Email from = new Email(fromEmail, fromName);
            Email toEmail = new Email(to);
            Content content = new Content("text/plain", body);
            Mail mail = new Mail(from, subject, toEmail, content);

            SendGrid sg = new SendGrid(apiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 400) {
                log.error("SendGrid error {} sending to {}: {}", response.getStatusCode(), to, response.getBody());
            } else {
                log.info("Email sent successfully to: {} (status: {})", to, response.getStatusCode());
            }
        } catch (IOException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}