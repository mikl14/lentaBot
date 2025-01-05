package com.telegrambot.lentaBot.service;


import com.telegrambot.lentaBot.config.BotConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class TelegramBot extends TelegramLongPollingBot {
    final BotConfig config;
    @Value("${bot.adminedChatId}")
    String adminedChatId;           //Id чата куда бот будет отправлять сообщения о записи

    //final TelegramClient telegramClient;

    public TelegramBot(BotConfig config) {
        this.config = config;

/*        this.telegramClient = telegramClient;

        // Замените на ваш номер телефона
        String phoneNumber = "+79164831404";
        telegramClient.login(phoneNumber);

        String code = "";
        telegramClient.checkCode(code);*/
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    /**
     * <b>getCallsBackDatas</b>
     * - метод который преобразовывает массив факультетов в callbackData по нужному формату
     *
     * @param Data
     * @return массив строк для callsBacksData
     */
    private String[] getCallsBackDatas(String[] Data) {
        List<String> callsBacks = new ArrayList<>();

        for (int i = 0; i < Data.length; i++) {
            callsBacks.add("\\call_" + Data[i]);
        }

        return callsBacks.toArray(new String[0]);
    }

    @Override
    public void onUpdateReceived(Update update) {
        //   ниже обработка кнопок с клавиатуры в панели и команд в сообщении!
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();

        }
        //   ниже обработка кнопок с клавиатур в сообщении!
        else if (update.hasCallbackQuery()) {

        }
    }

    /**
     * <b>send</b>
     * -отправляет в чат созданное сообщение с файлом
     *
     * @param messageObj
     */
    public void send(SendDocument messageObj) {
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

}


