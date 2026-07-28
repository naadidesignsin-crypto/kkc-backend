package com.kkc.kundali.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class KundaliOrderIdGenerator {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        return "KKC-" + LocalDate.now().format(DATE_FORMATTER) + "-" + randomSuffix(6);
    }

    private String randomSuffix(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            builder.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }
        return builder.toString();
    }
}
