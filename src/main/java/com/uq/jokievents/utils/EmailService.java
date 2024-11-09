package com.uq.jokievents.utils;

import com.uq.jokievents.model.Client;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Sends an email to a clients email from application.properties data (my gmail!!!)
     * @param to clients mail
     * @param verCode verification code of the client
     */
    public void sendVerificationMail(String to, String verCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Verification Email");
        message.setText("Please use the following code to activate your account: " + verCode);
        mailSender.send(message);
    }

    /**
     * Sends a recuperation email to a client to reset their password
     * @param to client's email
     * @param verCode the password reset link
     */
    public void sendRecuperationEmail(String to, String verCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Recuperation");
        message.setText("Please use the following code to create a new password: " + verCode);
        mailSender.send(message);
    }

}
