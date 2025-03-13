package com.telegrambot.lentaBot.bot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegrambot.lentaBot.bot.config.BotConfig;
import com.telegrambot.lentaBot.bot.entity.Chat;
import com.telegrambot.lentaBot.bot.requests.Status;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.NoSuchElementException;
import java.util.logging.Logger;

@Service
public class DataBaseRestService {
    Logger logger = Logger.getLogger(DataBaseRestService.class.getName());

    private final BotConfig config;
    private final RestTemplate restTemplate;

    private final ObjectMapper mapper;

    public DataBaseRestService(BotConfig config, RestTemplate restTemplate,ObjectMapper mapper) {
        this.config = config;
        this.restTemplate = restTemplate;
        this.mapper = mapper;
    }

    public String sendRequest(String prefix,String body)
    {
        StringBuilder sb = new StringBuilder(config.getDatabaseUrl() +prefix);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

        try {
            return restTemplate.postForObject(sb.toString(), requestEntity, String.class);

        } catch (Exception e) {
            logger.warning("trouble on send request to Base! " + e);
            throw new NoSuchElementException();
        }
    }

    public Chat getChatByChatId(Long ChatId)
    {
        try {
            return mapper.readValue(sendRequest("/getChatByChatId",ChatId.toString()),Chat.class);
        } catch (Exception e) {
            logger.warning("trouble on getUser! " + e);
            return null;
        }
    }
    /**
     * <b>sendJoinRequest</b> - отправляет запрос на вступление в открытую группу по ссылке
     *
     * @param chatId ссылка на открытый канал в формате @name
     * @return массив из 2х строк title и chatId
     */

    public boolean saveChat(Chat chat) {
        try {
            String request = sendRequest("/saveChat", String.valueOf(chat.getChatId()));
            return request.equals(Status.SUCCESS.toString());
        } catch (Exception e) {
            logger.warning("trouble on saveChat error to connect base! " + e);
            throw new NoSuchElementException();
        }
    }

}
