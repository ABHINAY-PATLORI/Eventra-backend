package com.college.service;

import com.college.exception.EmailDeliveryException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String mailUsername;
    private final String mailPassword;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.mail.from:}") String fromAddress,
                        @Value("${spring.mail.username:}") String mailUsername,
                        @Value("${spring.mail.password:}") String mailPassword) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.mailUsername = mailUsername;
        this.mailPassword = mailPassword;
    }

    /**
     * Validate email service configuration
     * Throws exception if mail sender is not properly configured
     */
    public void validateConfiguration() {
        if (!StringUtils.hasText(fromAddress) || !StringUtils.hasText(mailUsername) || !StringUtils.hasText(mailPassword)) {
            String errorMsg = String.format(
                "Email service is not properly configured. " +
                "Please set: app.mail.from='%s', spring.mail.username='%s', spring.mail.password='(app password)'",
                StringUtils.hasText(fromAddress) ? "✓ configured" : "NOT SET",
                StringUtils.hasText(mailUsername) ? "✓ configured" : "NOT SET"
            );
            log.error("❌ {}", errorMsg);
            throw new EmailDeliveryException(errorMsg);
        }
        log.info("✅ Email service configuration validated successfully");
    }

    /**
     * Verify email service is working
     * Useful for checking if SMTP connection is valid
     */
    public void verifyEmailService() {
        try {
            validateConfiguration();
            log.info("✅ Email service verified and ready to use");
        } catch (EmailDeliveryException e) {
            log.error("❌ Email service verification failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Send test email to verify configuration
     * @param testEmail Recipient email address
     */
    @Async
    public CompletableFuture<Void> sendTestEmail(String testEmail) {
        try {
            validateConfiguration();
            
            log.info("📧 Sending test email to {}", testEmail);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            
            helper.setTo(testEmail);
            helper.setFrom(fromAddress);
            helper.setSubject("✅ Test Email - Configuration Verified");
            helper.setText(buildTestEmailBody(), true);
            
            mailSender.send(message);
            log.info("✅ Test email sent successfully to {}", testEmail);
            return CompletableFuture.completedFuture(null);
        } catch (MailException | MessagingException ex) {
            log.error("❌ Failed to send test email to {}: {}", testEmail, ex.getMessage(), ex);
            return CompletableFuture.failedFuture(
                new EmailDeliveryException("Test email delivery failed: " + ex.getMessage(), ex)
            );
        }
    }

    /**
     * Send OTP email
     * @param recipientEmail Target email address
     * @param otp One-time password code
     */
    @Async
    public CompletableFuture<Void> sendOtpEmail(String recipientEmail, String otp) {
        try {
            validateConfiguration();

            log.info("📧 Sending OTP email to {}", recipientEmail);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            
            helper.setTo(recipientEmail);
            helper.setFrom(fromAddress);
            helper.setSubject("🔐 College Event Management - OTP Verification");
            helper.setText(buildOtpBody(otp), true);
            
            mailSender.send(message);
            log.info("✅ OTP email sent successfully to {}", recipientEmail);
            return CompletableFuture.completedFuture(null);
        } catch (MailException | MessagingException ex) {
            log.error("❌ Failed to send OTP email to {}: {}", recipientEmail, ex.getMessage(), ex);
            return CompletableFuture.failedFuture(
                new EmailDeliveryException("OTP email delivery failed: " + ex.getMessage(), ex)
            );
        }
    }

    /**
     * Send registration confirmation email
     * @param recipientEmail Target email address
     * @param userName User's name
     */
    @Async
    public CompletableFuture<Void> sendRegistrationConfirmationEmail(String recipientEmail, String userName) {
        try {
            validateConfiguration();

            log.info("📧 Sending registration confirmation email to {}", recipientEmail);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            
            helper.setTo(recipientEmail);
            helper.setFrom(fromAddress);
            helper.setSubject("✅ Registration Successful");
            helper.setText(buildRegistrationConfirmationBody(userName), true);
            
            mailSender.send(message);
            log.info("✅ Registration confirmation email sent to {}", recipientEmail);
            return CompletableFuture.completedFuture(null);
        } catch (MailException | MessagingException ex) {
            log.error("❌ Failed to send registration confirmation email to {}: {}", recipientEmail, ex.getMessage(), ex);
            return CompletableFuture.failedFuture(
                new EmailDeliveryException("Registration email delivery failed: " + ex.getMessage(), ex)
            );
        }
    }

    /**
     * Build HTML body for OTP email
     */
    private String buildOtpBody(String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f4f4f4; }
                        .container { max-width: 600px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; }
                        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; text-align: center; border-radius: 5px; }
                        .content { padding: 20px; text-align: center; }
                        .otp-box { background: #f0f0f0; border: 2px dashed #667eea; padding: 20px; margin: 20px 0; border-radius: 5px; }
                        .otp-code { font-size: 36px; font-weight: bold; color: #667eea; letter-spacing: 3px; }
                        .expiry { color: #666; font-size: 14px; margin-top: 10px; }
                        .warning { background: #fff3cd; border: 1px solid #ffc107; padding: 10px; margin: 15px 0; border-radius: 5px; }
                        .footer { color: #999; font-size: 12px; text-align: center; margin-top: 20px; border-top: 1px solid #eee; padding-top: 10px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🔐 OTP Verification</h1>
                        </div>
                        <div class="content">
                            <p>Hello,</p>
                            <p>Your One-Time Password (OTP) for College Event Management System is:</p>
                            <div class="otp-box">
                                <div class="otp-code">%s</div>
                                <div class="expiry">⏱️ This OTP will expire in 5 minutes</div>
                            </div>
                            <div class="warning">
                                <strong>⚠️ Security Notice:</strong><br>
                                • Never share this OTP with anyone<br>
                                • This OTP is valid for one-time use only<br>
                                • If you didn't request this, please ignore this email
                            </div>
                        </div>
                        <div class="footer">
                            <p>&copy; %d College Event Management System. All rights reserved.</p>
                            <p>This is an automated email. Please do not reply.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(otp, java.time.Year.now().getValue());
    }

    /**
     * Build HTML body for test email
     */
    private String buildTestEmailBody() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f4f4f4; }
                        .container { max-width: 600px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; }
                        .header { background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%); color: white; padding: 20px; text-align: center; border-radius: 5px; }
                        .content { padding: 20px; }
                        .success-box { background: #e8f5e9; border: 2px solid #4CAF50; padding: 15px; margin: 15px 0; border-radius: 5px; }
                        .details { background: #f5f5f5; padding: 15px; margin: 15px 0; border-radius: 5px; border-left: 4px solid #4CAF50; }
                        .footer { color: #999; font-size: 12px; text-align: center; margin-top: 20px; border-top: 1px solid #eee; padding-top: 10px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>✅ Email Configuration Verified</h1>
                        </div>
                        <div class="content">
                            <p>Hello,</p>
                            <div class="success-box">
                                <h2>🎉 Success!</h2>
                                <p>Your email service is working perfectly!</p>
                            </div>
                            <div class="details">
                                <h3>Email Details:</h3>
                                <ul>
                                    <li><strong>From:</strong> College Event Management System</li>
                                    <li><strong>Service:</strong> Gmail SMTP</li>
                                    <li><strong>Status:</strong> ✅ Operational</li>
                                    <li><strong>Timestamp:</strong> %s</li>
                                </ul>
                            </div>
                            <p>You can now use the email service for sending OTP codes and other notifications.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; %d College Event Management System. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(java.time.LocalDateTime.now(), java.time.Year.now().getValue());
    }

    /**
     * Build HTML body for registration confirmation email
     */
    private String buildRegistrationConfirmationBody(String userName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f4f4f4; }
                        .container { max-width: 600px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; }
                        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; text-align: center; border-radius: 5px; }
                        .content { padding: 20px; }
                        .footer { color: #999; font-size: 12px; text-align: center; margin-top: 20px; border-top: 1px solid #eee; padding-top: 10px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>✅ Registration Successful</h1>
                        </div>
                        <div class="content">
                            <p>Hello %s,</p>
                            <p>Thank you for registering in the College Event Management System!</p>
                            <p>Your account has been created and verified. You can now:</p>
                            <ul>
                                <li>Browse and register for events</li>
                                <li>Manage your event bookings</li>
                                <li>Receive event notifications</li>
                            </ul>
                            <p>Welcome to our community!</p>
                        </div>
                        <div class="footer">
                            <p>&copy; %d College Event Management System. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName, java.time.Year.now().getValue());
    }
}

