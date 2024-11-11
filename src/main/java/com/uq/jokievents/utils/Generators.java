package com.uq.jokievents.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Hashtable;
import java.util.Random;

public class Generators {

    public static String generateRndVerificationCode() {

        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(rnd.nextInt(10));
        }
        return sb.toString();
    }

    public static String generateQRCode(String text) throws Exception {
        // returns a base64 image of a qr
        Hashtable<EncodeHintType, String> hintMap = new Hashtable<>();
        hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, 300, 300, hintMap);

        BufferedImage image = new BufferedImage(matrix.getWidth(), matrix.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < matrix.getWidth(); i++) {
            for (int j = 0; j < matrix.getHeight(); j++) {
                image.setRGB(i, j, matrix.get(i, j) ? 0x000000 : 0xFFFFFF); // Black and white
            }
        }

        // Convert to Base64
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        return Base64.getEncoder().encodeToString(imageBytes);
    }

    public static BufferedImage generateQRCodeImage(String qrCodeData) throws Exception {
        // Set QR code encoding hints
        Hashtable<EncodeHintType, String> hintMap = new Hashtable<>();
        hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        // Create a multi-format writer for QR code generation
        MultiFormatWriter writer = new MultiFormatWriter();

        // Encode the data into a BitMatrix
        BitMatrix bitMatrix = writer.encode(qrCodeData, BarcodeFormat.QR_CODE, 300, 300, hintMap);

        // Create a BufferedImage based on the BitMatrix
        BufferedImage image = new BufferedImage(bitMatrix.getWidth(), bitMatrix.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < bitMatrix.getWidth(); x++) {
            for (int y = 0; y < bitMatrix.getHeight(); y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }

        return image;
    }
}
