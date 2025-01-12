package com.telegrambot.lentaBot.bot.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("application.properties")
public class BotConfig {
    @Getter
    @Value("${bot.name}")
    String botName;


    @Getter
    @Value("${bot.token}")
    String token;

    @Getter
    @Value("${bot.apiChatId}")
    Long apiChatId;

    @Getter
    @Value("${bot.apiUrl}")
    String apiUrl;
}
