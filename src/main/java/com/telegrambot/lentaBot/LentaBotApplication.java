package com.telegrambot.lentaBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class LentaBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(LentaBotApplication.class, args);
    }
}
