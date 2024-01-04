package com.zeliafinance.identitymanagement.service.impl;

import com.zeliafinance.identitymanagement.dto.EmailDetails;
import com.zeliafinance.identitymanagement.repository.UserCredentialRepository;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class HtmlTemplateService {
    final Configuration configuration;
    final JavaMailSender javaMailSender;
    final UserCredentialRepository userCredentialRepository;

    public void sendEmail(EmailDetails emailDetails) throws MessagingException, TemplateException, IOException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
        mimeMessageHelper.setTo(emailDetails.getRecipient());
        String emailContent = "";
        if (emailDetails.getType().equalsIgnoreCase("Login")){
            emailContent = getLoginContent(emailDetails);
            mimeMessageHelper.setSubject("ZELIA FINANCE LOG IN CONFIRMATION!");
        }
        if (emailDetails.getType().equalsIgnoreCase("Welcome")){
            emailContent = getWelcomeEmailContent();
            mimeMessageHelper.setSubject("Welcome To The Future");
        }
        if (emailDetails.getType().equalsIgnoreCase("Email Verification")){
            emailContent = getEmailContent(emailDetails);
            mimeMessageHelper.setSubject("Zelia Email Verification");
        }
        if (emailDetails.getType().equalsIgnoreCase("Password Reset")){
            emailContent = getResetPasswordContent(emailDetails);
            mimeMessageHelper.setSubject("Reset Your Password");
        }
        if (emailDetails.getType().equalsIgnoreCase("Loan Denial")){
            emailContent = getDenyLoanContent(emailDetails);
            mimeMessageHelper.setSubject("LOAN APPROVAL UPDATE");
        }
        if (emailDetails.getType().equalsIgnoreCase("Loan Approved")){
            emailContent = getLoanApprovedContent(emailDetails);
            mimeMessageHelper.setSubject("Congratulations! Loan Approved");
        }
        if (emailDetails.getType().equalsIgnoreCase("Loan Disbursed")){
            emailContent = getLoanDisbursedContent(emailDetails);
            mimeMessageHelper.setSubject("Loan Disbursement Successful");
        }
        mimeMessageHelper.setText(emailContent, true);
        mimeMessageHelper.setFrom("no-reply@zeliafinance.com", "Zee from  Zelia");
        log.info("message: {}", mimeMessageHelper.getMimeMessage().getContent());
        log.info(mimeMessageHelper.getMimeMessage().getSubject());
        javaMailSender.send(mimeMessage);
    }

    private String getEmailContent(EmailDetails emailDetails) throws IOException, TemplateException {
        StringWriter stringWriter = new StringWriter();
        Map<String, Object> model = new HashMap<>();
        model.put("otp", emailDetails.getMessageBody());
        configuration.getTemplate("otptemplate.ftlh").process(model, stringWriter);
        return stringWriter.getBuffer().toString();
    }

    private String getLoanApprovedContent(EmailDetails emailDetails) throws IOException, TemplateException {
        StringWriter stringWriter = new StringWriter();
        Map<String, Object> model = new HashMap<>();
        model.put("username", emailDetails.getMessageBody());
        configuration.getTemplate("loan-approved.ftlh").process(model, stringWriter);
        return stringWriter.getBuffer().toString();
    }

    private String getLoanDisbursedContent(EmailDetails emailDetails) throws IOException, TemplateException {
        StringWriter stringWriter = new StringWriter();
        Map<String, Object> model = new HashMap<>();
        model.put("username", emailDetails.getMessageBody());
        configuration.getTemplate("loan-disbursed.ftlh").process(model, stringWriter);
        return stringWriter.getBuffer().toString();
    }
    public String getResetPasswordContent(EmailDetails emailDetails) throws IOException, TemplateException {
        StringWriter stringWriter = new StringWriter();
        Map<String, Object> model = new HashMap<>();


        model.put("username", emailDetails.getMessageBody().substring(6));
        model.put("otp", emailDetails.getMessageBody().substring(0, 6));
        configuration.getTemplate("resetpassword.ftlh").process(model, stringWriter);
        return stringWriter.getBuffer().toString();
    }

    private String getLoginContent(EmailDetails emailDetails) throws IOException, TemplateException {
        StringWriter stringWriter = new StringWriter();
        Map<String, Object> model = new HashMap<>();
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        model.put("time", currentTime.format(formatter));
        model.put("username", emailDetails.getMessageBody());
        configuration.getTemplate("logintemplate.ftlh").process(model, stringWriter);
        return stringWriter.getBuffer().toString();
    }

    private String getDenyLoanContent(EmailDetails emailDetails) throws IOException, TemplateException {
        StringWriter stringWriter = new StringWriter();
        Map<String, Object> model = new HashMap<>();
        model.put("username", emailDetails.getMessageBody());
        configuration.getTemplate("loandenial.ftlh").process(model, stringWriter);
        return stringWriter.getBuffer().toString();
    }

    private String getWelcomeEmailContent() throws IOException, TemplateException {
        StringWriter stringWriter = new StringWriter();
        Map<String, Object> model = new HashMap<>();
        configuration.getTemplate("welcome.ftlh").process(model, stringWriter);
        return stringWriter.getBuffer().toString();
    }



}
