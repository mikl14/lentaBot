package com.telegrambot.lentaBot.bot.service;

import com.telegrambot.lentaBot.bot.config.BotConfig;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.util.logging.Logger;

@Service
public class RestService {
    private final RestTemplate restTemplate;

    private final BotConfig config;

    public RestService(RestTemplate restTemplate, BotConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    public String[] sendJoinRequest(String Body) {
        String url = config.getApiUrl();

        // Установка заголовков
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Создание тела запроса
        HttpEntity<String> requestEntity = new HttpEntity<>(Body, headers);

        try {
            // Отправка POST-запроса
            String response = restTemplate.postForObject(url, requestEntity, String.class);

            if(!response.equals("no_chat"))
            {
                return response.split(";");
            }
        }
        catch (Exception e)
        {
            System.out.println("Connect trouble " + e);
        }
        return null;
    }
}
