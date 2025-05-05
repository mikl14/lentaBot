package com.telegrambot.lentaBot.bot.service.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegrambot.lentaBot.bot.annotations.Logging;
import com.telegrambot.lentaBot.bot.config.BotConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.logging.Logger;

@Service
public class GatewayRestService extends RestService {
    private final RestTemplate restTemplate;

    private final BotConfig config;

    Logger logger = Logger.getLogger(GatewayRestService.class.getName());

    public GatewayRestService(RestTemplate restTemplate, ObjectMapper mapper, RestTemplate restTemplate1, BotConfig config) {
        super(restTemplate, mapper);
        this.restTemplate = restTemplate1;
        this.config = config;
    }

    /**
     * <b>sendJoinRequest</b> - отправляет запрос на вступление в открытую группу по ссылке
     *
     * @param Body ссылка на открытый канал в формате @name
     * @return массив из 2х строк title и chatId
     */

    @Logging(entering = true, returnData = true)
    public ResponseEntity<String> sendJoinRequest(String channelLink) {
        return sendRequest(config.getGatewayUrl() + "/joinChannel", Map.of("channelLink", channelLink), "");
    }

    /**
     * <b>sendPrivateJoinRequest</b> - отправляет запрос на вступление в закрытую группу по ссылке
     *
     * @param Body ссылка на открытый канал в формате ссылки приглашения
     * @return массив из 3х строк title , chatId и inviteLink
     */
    @Logging(entering = true, returnData = true)
    public ResponseEntity<String> sendPrivateJoinRequest(String channelLink) {
        return sendRequest(config.getGatewayUrl() + "/joinPrivateChat", Map.of("channelLink", channelLink), "");
    }


    /**
     * <b>sendJoinRequest</b> - отправляет запрос на вступление в открытую группу по ссылке
     *
     * @param chatId ссылка на открытый канал в формате @name
     * @return массив из 2х строк title и chatId
     */
    @Logging(entering = true, returnData = true)
    public ResponseEntity<String> sendLeaveRequest(String channelLink) {

        return sendRequest(config.getGatewayUrl() + "/leaveChat", Map.of("channelLink", channelLink), "");
    }

}
