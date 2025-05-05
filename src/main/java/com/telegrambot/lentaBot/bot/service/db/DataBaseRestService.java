package com.telegrambot.lentaBot.bot.service.db;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegrambot.lentaBot.bot.annotations.Logging;
import com.telegrambot.lentaBot.bot.config.BotConfig;
import com.telegrambot.lentaBot.bot.entity.Channel;
import com.telegrambot.lentaBot.bot.entity.Chat;
import com.telegrambot.lentaBot.bot.requests.Status;
import com.telegrambot.lentaBot.bot.service.rest.RestService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

@Service
public class DataBaseRestService extends RestService {
    Logger logger = Logger.getLogger(DataBaseRestService.class.getName());

    private final BotConfig config;
    private final RestTemplate restTemplate;

    private final ObjectMapper mapper;

    public DataBaseRestService(RestTemplate restTemplate, ObjectMapper mapper, BotConfig config, RestTemplate restTemplate1, ObjectMapper mapper1) {
        super(restTemplate, mapper);
        this.config = config;
        this.restTemplate = restTemplate1;
        this.mapper = mapper1;
    }

    /**
     * <b>getChatByChatId</b> - возвращает объект Chat по chatId
     *
     * @param chatId
     * @return
     */
    @Logging(entering = true, exiting = true, returnData = true)
    public Chat getChatByChatId(Long chatId) {
        try {
            return sendRequest(config.getDatabaseUrl()+ "/getChatByChatId", Map.of("chatId", chatId.toString()), "", Chat.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * <b>saveChat</b> - сохраняет Chat в базу данных
     *
     * @return Https.status
     */
    @Logging(entering = true, exiting = true, returnData = true)
    public boolean saveChat(Chat chat) {
        try {
            ResponseEntity<String> responseEntity = sendRequest(config.getDatabaseUrl()+"/saveChat", new HashMap<>(), mapper.writeValueAsString(chat));
            return responseEntity.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            throw new NoSuchElementException();
        }
    }

    /**
     * <b>deleteChat</b> - удаляет чат
     */
    @Logging(entering = true, exiting = true, returnData = true)
    public boolean deleteChat(Long chatId) {
        try {
            ResponseEntity<String> responseEntity =  sendRequest(config.getDatabaseUrl()+"/deleteChat", Map.of("chatId", chatId.toString()), "");
            return responseEntity.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            throw new NoSuchElementException();
        }
    }


    /**
     * <b>getAllChatIdOfChats</b> - возвращает все ChatId существующих чатов
     *
     * @return List Long
     */
    @Logging(entering = true, exiting = true, returnData = true)
    public List<Long> getAllChatIdOfChats() {
        try {
            return sendRequestWithList(config.getDatabaseUrl()+"/getAllChatIdOfChats", new HashMap<>(), "", Long.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * <b>addChannelInChatByChatId</b> - Добавляет канал в чат с созданием всех связей
     *
     * @param chat    чат в который добавляется канал
     * @param channel добавляемый канал
     * @return result true/false
     */
    @Logging(entering = true, exiting = true, returnData = true)
    public boolean addChannelInChatByChatId(Chat chat, Channel channel) {
        try {
            ResponseEntity<String> responseEntity =  sendRequest(config.getDatabaseUrl()+"/addChannelInChatByChatId", Map.of("chatId", chat.getChatId().toString()), mapper.writeValueAsString(channel));
            return responseEntity.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            throw new NoSuchElementException();
        }
    }

    /**
     * <b>removeChannelFromChat</b> - удаляет канал из чата
     *
     * @param chat
     * @param channel
     * @return result true/false
     */
    @Logging(entering = true, exiting = true, returnData = true)
    public boolean removeChannelFromChat(Chat chat, Channel channel) {
        try {
            ResponseEntity<String> responseEntity = sendRequest(config.getDatabaseUrl()+"/removeChannelFromChat", Map.of("chatId", chat.getChatId().toString(), "channelChatId", channel.getChatId().toString()), "");
            return responseEntity.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            throw new NoSuchElementException();
        }
    }

    /**
     * <b>getChatChannelsByChatId</b> - возвращает список всех каналов в чате по ChatId
     *
     * @param chatId chatId чата
     * @return List(Channel)
     */
    @Logging(entering = true, exiting = true, returnData = true)
    public List<Channel> getChatChannelsByChatId(Long chatId) {
        try {
            return sendRequestWithList(config.getDatabaseUrl()+"/getChatChannelsByChatId", Map.of("chatId", chatId.toString()), "", Channel.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * <b>getChannelByChatId</b> - возвращает Channel по его chatId
     *
     * @param chatId
     * @return
     */
    @Logging(entering = true, exiting = true, returnData = true)
    public Channel getChannelByChatId(Long chatId) {
        try {
            return sendRequest(config.getDatabaseUrl()+"/getChannelByChatId", Map.of("chatId", chatId.toString()), "", Channel.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * <b>getChannelChatsByChatId</b> - возвращает список всех чатов из канала по ChatId
     *
     * @param chatId chatId канала
     * @return List(Chat)
     */
    @Logging(entering = true, exiting = true, returnData = true)
    public List<Chat> getChannelChatsByChatId(Long chatId) {
        try {
            return sendRequestWithList(config.getDatabaseUrl()+"/getChannelChatsByChatId", Map.of("chatId", chatId.toString()), "",Chat.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * <b>getChannelByInviteLink</b> - возвращает объект Channel по inviteLink
     *
     * @param inviteLink ссылка приглашение в канала
     * @return
     */
    @Logging(entering = true, exiting = true, returnData = true)
    public Channel getChannelByInviteLink(String inviteLink) {
        try {
            return sendRequest(config.getDatabaseUrl()+"/getChannelByInviteLink", Map.of("inviteLink", inviteLink), "", Channel.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * <b>deleteChannel</b> - удаляет канал
     */
    @Logging(entering = true, exiting = true, returnData = true)
    public boolean deleteChannel(Long chatId) {
        try {
            ResponseEntity<String> responseEntity = sendRequest(config.getDatabaseUrl()+"/deleteChannel", Map.of("chatId", chatId.toString()), "");
            return responseEntity.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            throw new NoSuchElementException();
        }
    }

    /**
     * <b>getChannelRating</b> - возвращает 5 наиболее популярных каналов
     *
     * @return List(Channel)
     */
    @Logging(entering = true, exiting = true, returnData = true)
    public List<Channel> getChannelRating() {
        try {
            return sendRequestWithList(config.getDatabaseUrl()+"/getChannelRating", new HashMap<>(), "", Channel.class);
        } catch (Exception e) {
            throw new NoSuchElementException();
        }
    }

}
