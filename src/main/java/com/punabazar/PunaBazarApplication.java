package com.punabazar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PunaBazarApplication {

    public static void main(String[] args) {
        System.setProperty("spring.jpa.database-platform", "org.hibernate.dialect.MySQLDialect");
        System.setProperty("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        System.setProperty("spring.datasource.driverClassName", "com.mysql.cj.jdbc.Driver");

        SpringApplication.run(PunaBazarApplication.class, args);
        System.out.println("==========================================================");
        System.out.println("🚩 Pune Bazar WhatsApp Calculator & Ledger System Started!");
        System.out.println("🌐 URL: http://localhost:8085");
        System.out.println("🔑 Default Admin: admin / admin123");
        System.out.println("==========================================================");
    }
}
