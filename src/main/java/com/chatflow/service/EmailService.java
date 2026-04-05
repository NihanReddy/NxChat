package com.chatflow.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean sendOtpEmail(String to, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            
            // 2. This is the user trying to log in
            helper.setTo(to); 
            
            helper.setSubject("Your Access Key | Atmospheric Precision");
            
            String htmlContent = 
                "<html><body style='font-family: Arial, sans-serif; background-color: #001212; color: #ffffff; padding: 40px;'>" +
                "<div style='max-width: 600px; margin: auto; background: #012324; padding: 40px; border-radius: 24px; border: 1px solid rgba(255,255,255,0.1);'>" +
                "<h1 style='color: #40f2fe; margin-bottom: 24px;'>NxChat</h1>" +
                "<p style='font-size: 16px; color: rgba(255,255,255,0.7);'>Experience the next evolution of digital intimacy. Enter the matching code to gain access:</p>" +
                "<div style='background: rgba(255,255,255,0.05); padding: 24px; border-radius: 12px; text-align: center; margin: 32px 0;'>" +
                "<span style='font-size: 48px; font-weight: 800; letter-spacing: 12px; color: #40f2fe;'>" + otp + "</span>" +
                "</div>" +
                "<p style='font-size: 14px; color: rgba(255,255,255,0.5);'>This code expires in 5 minutes. If you didn't request this, you can safely ignore this email.</p>" +
                "<hr style='border: 0; border-top: 1px solid rgba(255,255,255,0.1); margin: 32px 0;'>" +
                "<p style='font-size: 12px; color: rgba(255,255,255,0.3);'>&copy; 2024 NxChat. Atmospheric Precision.</p>" +
                "</div></body></html>";
                
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("OTP email sent to {}", to);
            return true;
        } catch (MessagingException | MailException e) {
            log.error("Failed to send OTP email to {}", to, e);
            return false;
        } catch (Exception e) {
            log.error("Unexpected failure while sending OTP email to {}", to, e);
            return false;
        }
    }
}
