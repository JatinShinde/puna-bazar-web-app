package com.punabazar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PunaBazarApplication {

    public static void main(String[] args) {
        SpringApplication.run(PunaBazarApplication.class, args);
        System.out.println("==========================================================");
        System.out.println("🚩 Pune Bazar WhatsApp Calculator & Ledger System Started!");
        System.out.println("🌐 URL: http://localhost:8086");
        System.out.println("🔑 Default Admin: admin / admin123");
        System.out.println("==========================================================");
    }
}
