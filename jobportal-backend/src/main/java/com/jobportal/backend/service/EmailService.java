package com.jobportal.backend.service;

import com.jobportal.backend.dto.*;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Autowired(required = false)
    private MeterRegistry meterRegistry;

    @Value("${app.email.from:noreply@jobportal.com}")
    private String fromEmail;

    @Value("${app.email.from-name:JobPortal}")
    private String fromName;

    @Value("${app.email.reply-to:support@jobportal.com}")
    private String replyTo;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${app.email.test-mode:false}")
    private boolean testMode;

    @Value("${app.email.max-retries:3}")
    private int maxRetries;

    @Value("${app.email.retry-delay:5000}")
    private long retryDelay;

    /**
     * Send email asynchronously
     */
    @Async("emailTaskExecutor")
    public void sendEmail(EmailRequest emailRequest) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Skipping email to: {}", emailRequest.getTo());
            return;
        }

        try {
            log.info("Sending email to: {} with template: {}", emailRequest.getTo(), emailRequest.getTemplate());
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(emailRequest.getTo());
            helper.setSubject(emailRequest.getSubject());

            // Generate HTML content from template
            String htmlContent = generateHtmlContent(emailRequest.getTemplate(), emailRequest.getTemplateData());
            helper.setText(htmlContent, true);

            mailSender.send(message);
            
            log.info("Email sent successfully to: {}", emailRequest.getTo());
            
        } catch (MessagingException e) {
            log.error("Failed to create email message for: {}", emailRequest.getTo(), e);
        } catch (MailException e) {
            log.error("Failed to send email to: {}", emailRequest.getTo(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to: {}", emailRequest.getTo(), e);
        }
    }

    /**
     * Send welcome email to new users
     */
    @Async("emailTaskExecutor")
    public void sendWelcomeEmail(String userEmail, WelcomeEmailData welcomeData) {
        log.info("Sending welcome email to: {}", userEmail);
        
        EmailRequest emailRequest = EmailRequest.builder()
                .to(userEmail)
                .subject("Welcome to JobPortal - Your Account is Ready!")
                .template("welcome-email")
                .templateData(welcomeData)
                .priority("HIGH")
                .build();

        sendEmail(emailRequest);
    }

    /**
     * Send job notification email to candidates
     */
    @Async("emailTaskExecutor")
    public void sendJobNotificationEmail(String candidateEmail, JobNotificationEmailData jobData) {
        log.info("Sending job notification email to: {}", candidateEmail);
        
        EmailRequest emailRequest = EmailRequest.builder()
                .to(candidateEmail)
                .subject("New Job Match: " + jobData.getJobTitle() + " at " + jobData.getCompanyName())
                .template("job-notification-email")
                .templateData(jobData)
                .priority("MEDIUM")
                .build();

        sendEmail(emailRequest);
    }

    /**
     * Send application status update email
     */
    @Async("emailTaskExecutor")
    public void sendApplicationStatusEmail(String candidateEmail, ApplicationStatusEmailData statusData) {
        log.info("Sending application status email to: {} for status: {}", candidateEmail, statusData.getStatus());
        
        String subject = getApplicationStatusSubject(statusData.getStatus(), statusData.getJobTitle());
        
        EmailRequest emailRequest = EmailRequest.builder()
                .to(candidateEmail)
                .subject(subject)
                .template("application-status-email")
                .templateData(statusData)
                .priority("HIGH")
                .build();

        sendEmail(emailRequest);
    }

    /**
     * Send password reset email
     */
    @Async("emailTaskExecutor")
    public void sendPasswordResetEmail(String userEmail, String resetToken) {
        log.info("Sending password reset email to: {}", userEmail);
        
        PasswordResetEmailData resetData = PasswordResetEmailData.builder()
                .email(userEmail)
                .resetToken(resetToken)
                .expirationTime(LocalDateTime.now().plusHours(1))
                .resetUrl("http://localhost:3000/reset-password?token=" + resetToken)
                .build();

        EmailRequest emailRequest = EmailRequest.builder()
                .to(userEmail)
                .subject("Reset Your JobPortal Password")
                .template("password-reset-email")
                .templateData(resetData)
                .priority("HIGH")
                .build();

        sendEmail(emailRequest);
    }

    /**
     * Send recruiter notification when candidate applies
     */
    @Async("emailTaskExecutor")
    public void sendRecruiterNotificationEmail(String recruiterEmail, String candidateName, String jobTitle) {
        log.info("Sending recruiter notification email to: {} for candidate: {}", recruiterEmail, candidateName);
        
        RecruiterNotificationEmailData notificationData = RecruiterNotificationEmailData.builder()
                .recruiterEmail(recruiterEmail)
                .candidateName(candidateName)
                .jobTitle(jobTitle)
                .applicationDate(LocalDateTime.now())
                .dashboardUrl("http://localhost:3000/recruiter/dashboard")
                .build();

        EmailRequest emailRequest = EmailRequest.builder()
                .to(recruiterEmail)
                .subject("New Application Received: " + candidateName + " for " + jobTitle)
                .template("recruiter-notification-email")
                .templateData(notificationData)
                .priority("MEDIUM")
                .build();

        sendEmail(emailRequest);
    }

    /**
     * Generate HTML content from Thymeleaf template
     */
    private String generateHtmlContent(String templateName, Object templateData) {
        Context context = new Context();
        context.setVariable("data", templateData);
        context.setVariable("currentYear", LocalDateTime.now().getYear());
        context.setVariable("companyName", "JobPortal");
        
        return templateEngine.process(templateName, context);
    }

    /**
     * Get appropriate subject line for application status
     */
    private String getApplicationStatusSubject(String status, String jobTitle) {
        return switch (status.toUpperCase()) {
            case "RECEIVED" -> "Application Received: " + jobTitle;
            case "UNDER_REVIEW" -> "Application Under Review: " + jobTitle;
            case "INTERVIEW_SCHEDULED" -> "Interview Scheduled: " + jobTitle;
            case "ACCEPTED" -> "Congratulations! Job Offer: " + jobTitle;
            case "REJECTED" -> "Application Update: " + jobTitle;
            default -> "Application Status Update: " + jobTitle;
        };
    }
}
