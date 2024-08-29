package com.uq.jokievents.utils;

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
}
