package com.telegrambot.lentaBot.bot.service;

import com.telegrambot.lentaBot.bot.annotations.Logging;
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

    Logger logger = Logger.getLogger(RestService.class.getName());

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

    @Logging(entering = true,returnData = true)
    public String[] sendJoinRequest(String Body) {

        String url = config.getApiUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(Body, headers);

        logger.info("sending join request in channel: " + Body);
        try {
            String response = restTemplate.postForObject(url+"/joinChat", requestEntity, String.class);

            if (!response.equals("no_chat")) {
                return response.split(";");
            }
        } catch (Exception e) {
            logger.warning("Connect with telegram user service is trouble!");
        }
        logger.warning("Chat with name " + Body + " can't be found!");
        return null;
    }

    /**
     * <b>sendPrivateJoinRequest</b> - отправляет запрос на вступление в закрытую группу по ссылке
     *
     * @param Body ссылка на открытый канал в формате ссылки приглашения
     * @return массив из 3х строк title , chatId и inviteLink
     */
    @Logging(entering = true,returnData = true)
    public String[] sendPrivateJoinRequest(String Body) {

        String url = config.getApiUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(Body, headers);

        logger.info("sending join request in private channel with link: " + Body);
        try {
            String response = restTemplate.postForObject(url+"/joinPrivateChat", requestEntity, String.class);

            if (!response.equals("chat_is_already")) {
                return response.split(";");
            }
            else
            {
                logger.warning("Private chat is already joined " + Body + " fatal!");
            }
        } catch (Exception e) {
            logger.warning("Connect with telegram user service is trouble!");
        }
        logger.warning("Chat with link " + Body + " can't be found!");
        return null;
    }


    /**
     * <b>sendJoinRequest</b> - отправляет запрос на вступление в открытую группу по ссылке
     *
     * @param chatId ссылка на открытый канал в формате @name
     * @return массив из 2х строк title и chatId
     */
    @Logging(entering = true,returnData = true)
    public String[] sendLeaveRequest(Long chatId) {

        String id = chatId.toString();
        String url = config.getApiUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(id, headers);

        logger.info("sending join request in channel: " + id);
        try {
            String response = restTemplate.postForObject(url+"/leaveChat", requestEntity, String.class);

            if (!response.equals("no_chat")) {
                return response.split(";");
            }
        } catch (Exception e) {
            logger.warning("Connect with telegram user service is trouble!");
        }
        logger.warning("Chat with name " + id + " can't be found!");
        return null;
    }

}
