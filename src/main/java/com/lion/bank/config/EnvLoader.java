package com.lion.bank.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class EnvLoader {

    @PostConstruct
    public void loadEnv() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        if (dotenv != null) {
            setSystemPropertyIfAbsent("DB_URL", dotenv.get("DB_URL"));
            setSystemPropertyIfAbsent("DB_USERNAME", dotenv.get("DB_USERNAME"));
            setSystemPropertyIfAbsent("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
            setSystemPropertyIfAbsent("JWT_SECRET", dotenv.get("JWT_SECRET"));
            setSystemPropertyIfAbsent("RESEND_API_KEY", dotenv.get("RESEND_API_KEY"));
        }
    }

    private void setSystemPropertyIfAbsent(String key, String value) {
        if (value != null && System.getProperty(key) == null && System.getenv(key) == null) {
            System.setProperty(key, value);
        }
    }
}
