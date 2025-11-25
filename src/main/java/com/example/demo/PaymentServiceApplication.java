package com.example.demo;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PaymentServiceApplication {

    public static void main(String[] args) {

        // Load .env before Spring starts
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        // Inject into System properties so Spring can read them
        System.setProperty("RAZORPAY_KEY", dotenv.get("RAZORPAY_KEY"));
        System.setProperty("RAZORPAY_SECRET", dotenv.get("RAZORPAY_SECRET"));
        System.setProperty("RAZORPAY_WEBHOOK_SECRET", dotenv.get("RAZORPAY_WEBHOOK_SECRET"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));

        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
