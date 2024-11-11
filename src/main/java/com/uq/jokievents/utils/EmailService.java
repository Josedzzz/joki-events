package com.uq.jokievents.utils;

import com.uq.jokievents.exceptions.LogicException;
import com.uq.jokievents.model.Coupon;
import com.uq.jokievents.model.enums.CouponType;
import com.uq.jokievents.repository.CouponRepository;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class EmailService {

    private final JavaMailSender mailSender;
    private final CouponRepository couponRepository;

    /**
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

    public void sendDiscountCouponMail(String to) {

        List<Coupon> individualCoupons = couponRepository.findByCouponType(CouponType.INDIVIDUAL);
        Random random = new Random();
        Coupon randomCoupon = individualCoupons.get(random.nextInt(individualCoupons.size()));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Discount Coupon for your new Account");
        message.setText("Use this coupon: " + randomCoupon.getName() + " to get a " + randomCoupon.getDiscountPercent() + "% discount.\nAvailable for purchases that cost more than $" + randomCoupon.getMinPurchaseAmount() + " until " + randomCoupon.getExpirationDate() + "\n Be smart and buy!");
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

    /**
     * Sends an email with a QR that contains the recent Purchase information
     * @param to String
     * @param subject String
     * @param body String
     */
    public void sendPurchaseEmail(String to, String subject, String body, String base64QRCode) {
        try {
            // Decode the base64 QR code image
            byte[] imageBytes = Base64.getDecoder().decode(base64QRCode); // Extract the base64 data from the URI

            // Create MimeMessage
            MimeMessage message = mailSender.createMimeMessage();

            // Prepare the helper for the MimeMessage
            MimeMessageHelper helper = new MimeMessageHelper(message, true); // 'true' allows attachments

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // 'true' indicates that the body is HTML

            // Convert the byte array image into a DataSource for the inline attachment
            ByteArrayDataSource dataSource = new ByteArrayDataSource(imageBytes, "image/png");

            // Add the image as an inline attachment with a Content-ID
            helper.addInline("qrCodeImage", dataSource);

            // Send the email
            mailSender.send(message);
        } catch (Exception e) {
            throw new LogicException("Error sending email with embedded image " + e.getMessage(), e);
        }
    }
}
