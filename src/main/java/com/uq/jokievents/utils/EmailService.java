package com.uq.jokievents.utils;

import com.uq.jokievents.model.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

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
}
