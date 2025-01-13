package com.telegrambot.lentaBot.bot.service;

import com.telegrambot.lentaBot.bot.config.BotConfig;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.logging.Logger;

@Service
public class RestService {
    private final RestTemplate restTemplate;

    private final BotConfig config;

    Logger logger = Logger.getLogger(TelegramBot.class.getName());

    public RestService(RestTemplate restTemplate, BotConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    /**
     * <b>sendJoinRequest</b> - отправляет запрос на вступление в открытую группу по ссылке
     *
     * @param Body ссылка на открытый канал в формате @name
     * @return массив из 2х строк title и chatId
     */

    public String[] sendJoinRequest(String Body) {

        String url = config.getApiUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(Body, headers);

        logger.info("sending join request in channel: " + Body);
        try {
            String response = restTemplate.postForObject(url, requestEntity, String.class);

            if (!response.equals("no_chat")) {
                return response.split(";");
            }
        } catch (Exception e) {
            logger.warning("Connect with telegram user service is trouble!");
        }
        logger.warning("Chat with name " + Body + " can't be found!");
        return null;
    }
}
