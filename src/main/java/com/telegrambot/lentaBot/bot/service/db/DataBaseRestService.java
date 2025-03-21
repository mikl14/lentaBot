package com.telegrambot.lentaBot.bot.service.db;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegrambot.lentaBot.bot.config.BotConfig;
import com.telegrambot.lentaBot.bot.entity.Channel;
import com.telegrambot.lentaBot.bot.entity.Chat;
import com.telegrambot.lentaBot.bot.requests.Status;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
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

    public String sendRequest(String prefix, Map<String,String> headers, String body)
    {
        StringBuilder sb = new StringBuilder(config.getDatabaseUrl() +prefix);

        HttpHeaders reqHeaders = new HttpHeaders();
        reqHeaders.setContentType(MediaType.APPLICATION_JSON);
        for(Map.Entry<String, String> entry : headers.entrySet())
        {
            reqHeaders.add(entry.getKey(),entry.getValue());
        }


        HttpEntity<String> requestEntity = new HttpEntity<>(body, reqHeaders);

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
            return mapper.readValue(sendRequest("/getChatByChatId",Map.of("chatId",ChatId.toString()),""),Chat.class);
        } catch (Exception e) {
            logger.warning("trouble on getUser! " + e);
            return null;
        }
    }

    public Channel getChannelByChatId(Long ChatId)
    {
        try {
            return mapper.readValue(sendRequest("/getChannelByChatId",Map.of("chatId",ChatId.toString()),""),Channel.class);
        } catch (Exception e) {
            logger.warning("trouble on getUser! " + e);
            return null;
        }
    }


    public Channel getChannelByInviteLink(String inviteLink)
    {
        try {
            return mapper.readValue(sendRequest("/getChannelByInviteLink",Map.of("inviteLink",inviteLink),""),Channel.class);
        } catch (Exception e) {
            logger.warning("trouble on getUser! " + e);
            return null;
        }
    }

    public List<Long> getAllChatIdOfChats()
    {
        try {
            return mapper.readValue(sendRequest("/getAllChatIdOfChats",new HashMap<>(),""),new TypeReference<List<Long>>(){});
        } catch (Exception e) {
            logger.warning("trouble on getUser! " + e);
            return null;
        }
    }

    public List<Channel> getChatChannelsByChatId(Long ChatId)
    {
        try {
            return mapper.readValue(sendRequest("/getChatChannelsByChatId",Map.of("chatId",ChatId.toString()),""), new TypeReference<List<Channel>>(){});
        } catch (Exception e) {
            logger.warning("trouble on getUser! " + e);
            return null;
        }
    }

    public List<Chat> getChannelChatsByChatId(Long ChatId)
    {
        try {
            return mapper.readValue(sendRequest("/getChannelChatsByChatId",Map.of("chatId",ChatId.toString()),""), new TypeReference<List<Chat>>(){});
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
            String request = sendRequest("/saveChat",new HashMap<>(), mapper.writeValueAsString(chat));
            return request.equals(Status.SUCCESS.toString());
        } catch (Exception e) {
            logger.warning("trouble on saveChat error to connect base! " + e);
            throw new NoSuchElementException();
        }
    }

    public boolean deleteChannel(Long chatId) {
        try {
            String request = sendRequest("/deleteChannel",Map.of("chatId",chatId.toString()),"");
            return request.equals(Status.SUCCESS.toString());
        } catch (Exception e) {
            logger.warning("trouble on saveChat error to connect base! " + e);
            throw new NoSuchElementException();
        }
    }

    public boolean addChannelInChatByChatId(Chat chat,Channel channel) {
        try {
            String request = sendRequest("/addChannelInChatByChatId",Map.of("chatId",chat.getChatId().toString()), mapper.writeValueAsString(channel));
            return request.equals(Status.SUCCESS.toString());
        } catch (Exception e) {
            logger.warning("trouble on saveChat error to connect base! " + e);
            throw new NoSuchElementException();
        }
    }

    public boolean removeChannelFromChat(Chat chat,Channel channel) {
        try {
            String request = sendRequest("/removeChannelFromChat",Map.of("chatId",chat.getChatId().toString(),"channelChatId",channel.getChatId().toString()),"");
            return request.equals(Status.SUCCESS.toString());
        } catch (Exception e) {
            logger.warning("trouble on saveChat error to connect base! " + e);
            throw new NoSuchElementException();
        }
    }

    public List<Channel> getChannelRating ()
    {
        try {
            return mapper.readValue(sendRequest("/getChannelRating", new HashMap<>(), ""), new TypeReference<List<Channel>>(){});
        } catch (Exception e) {
            logger.warning("trouble on saveChat error to connect base! " + e);
            throw new NoSuchElementException();
        }
    }

}
