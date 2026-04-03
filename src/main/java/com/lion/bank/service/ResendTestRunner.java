package com.lion.bank.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ResendTestRunner implements CommandLineRunner {

    private final String apiKey;

    public ResendTestRunner(@Value("${resend.api.key}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("DEBUG: RESEND_API_KEY from @Value: " + apiKey);
        System.out.println("DEBUG: RESEND_API_KEY from System Property: " + System.getProperty("RESEND_API_KEY"));
        System.out.println("DEBUG: RESEND_API_KEY from Environment: " + System.getenv("RESEND_API_KEY"));
    }
}
