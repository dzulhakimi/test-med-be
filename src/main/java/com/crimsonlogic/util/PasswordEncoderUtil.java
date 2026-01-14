package com.crimsonlogic.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderUtil {
    public static void main(String[] args) {
        String rawPassword = "admin123";
        String hashed = new BCryptPasswordEncoder().encode(rawPassword);
        System.out.println("Encoded password: " + hashed);
        System.out.println(new BCryptPasswordEncoder().encode("admin123"));

    }
}
