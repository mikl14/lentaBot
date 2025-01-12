package com.telegrambot.lentaBot.bot.service;


import com.telegrambot.lentaBot.bot.config.BotConfig;
import com.telegrambot.lentaBot.bot.entity.Channel;
import com.telegrambot.lentaBot.bot.entity.Chat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    final BotConfig config;
    final ChatService chatService;

    final ChannelService channelService;

    final RestService restService;



    public TelegramBot(BotConfig config,ChatService chatService,RestService restService, ChannelService channelService) {
        this.config = config;
        this.chatService = chatService;
        this.restService = restService;
        this.channelService = channelService;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }



    @Override
    public void onUpdateReceived(Update update) {
        //   ниже обработка кнопок с клавиатуры в панели и команд в сообщении!
        var message = update.getMessage();
        if (update.hasMessage()) {
            Chat chat = chatService.findChat(message.getChatId());
            if(chat == null)
            {
               chat = new Chat(message.getChatId(),new ArrayList<>());
               chatService.saveChat(chat);
            }

            long chatId = message.getChatId();
            if(chatId != config.getApiChatId()) {
                if (message.hasText()) {
                    String messageText = message.getText();
                    deleteMessage(update.getMessage());
                    if(messageText.startsWith("@"))
                    {
                        String[] response = restService.sendJoinRequest(messageText);
                        Channel channel = channelService.findChannel(Long.parseLong(response[1]));
                        if(channel == null) {
                            chat.addChannel(new Channel(Long.parseLong(response[1]), response[0], List.of(chat)));

                        }
                        else {
                            channel.addChat(chat);
                            chat.addChannel(channel);
                        }
                        chatService.saveChat(chat);
                        send(BotMessageService.CreateMessage(chatId, "Ваша подписка на канал " + response[0] + " оформлена"));

                    }
                    switch (messageText) {
                        case "/start":
                        case "/info":
                            send(BotMessageService.CreateMessage(chatId, "Я Лента бот, и сейчас нахожусь в разработке"));

                            break;
                        case "/subs":
                            StringBuilder sb = new StringBuilder();

                            for(Channel channel: chat.getChanelList())
                            {
                                sb.append('\n');
                                sb.append(channel.getTitle());
                            }

                            send(BotMessageService.CreateMessage(chatId, "Ваши подписки:\n" + sb.toString() ));
                            break;

                    }
                }
            }
            else {

                Channel channel = channelService.findChannel(update.getMessage().getForwardFromChat().getId());
                ForwardMessage forwardMessage = new ForwardMessage();

                for(Chat channelChat : channel.getChats())
                {
                    forwardMessage.setChatId(channelChat.getChatId());
                    forwardMessage.setFromChatId(update.getMessage().getChatId());
                    forwardMessage.setMessageId(update.getMessage().getMessageId());
                    send(forwardMessage);
                }


            }

        }
        //   ниже обработка кнопок с клавиатур в сообщении!
        else if (update.hasCallbackQuery()) {
            String query = update.getCallbackQuery().getData();
            long ChatId = update.getCallbackQuery().getMessage().getChatId();


        }
    }


    public void deleteMessage(Message mes) {
        DeleteMessage delMes = new DeleteMessage();
        delMes.setChatId(String.valueOf(mes.getChatId()));
        delMes.setMessageId(mes.getMessageId());
        send(delMes);
    }

    /**
     * <b>send</b>
     * -отправляет в чат созданное сообщение с файлом
     *
     * @param messageObj
     */
    public void send(ForwardMessage messageObj) {
        try {
            execute(messageObj);
        } catch (TelegramApiException ex) {
            throw new RuntimeException(ex);
        }
    }


    /**
     * <b>send</b>
     * -отправляет в чат созданное сообщение с файлом
     *
     * @param messageObj
     */
    public void send(SendMessage messageObj) {
        try {
            execute(messageObj);
        } catch (TelegramApiException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * <b>send</b>
     * - реализует удаление заданного в обьекте сообщения
     *
     * @param messageObj
     */
    public void send(DeleteMessage messageObj) {
        try {
            execute(messageObj);
        } catch (TelegramApiException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void send(EditMessageText messageObj) {
        try {
            execute(messageObj);
        } catch (TelegramApiException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void send(BotApiMethod<Serializable> messageObj) {
        try {
            execute(messageObj);
        } catch (TelegramApiException ex) {
            throw new RuntimeException(ex);
        }
    }


    /**
     * <b>calculateTimeDifferenceInMinutes</b>
     * -считает разницу между временем в минутах
     *
     * @params time1 time2
     */
    public static long calculateTimeDifferenceInMinutes(LocalTime time1, LocalTime time2) {
        return ChronoUnit.MINUTES.between(time1, time2);
    }

    /**
     * <b>clearUsersSessions</b>
     * будет вызван каждые 15 минут с момента запуска приложения
     * отчищает все сессии которые не активны более 15 минут
     */
    @Scheduled(fixedRate = 1000 * 60 * 2) // 2 минуты
    public void clearUsersSessions() {


    }
}


