package com.telegrambot.lentaBot.bot.service;


import com.telegrambot.lentaBot.bot.config.BotConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
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
    @Value("${bot.adminedChatId}")
    String adminedChatId;           //Id чата куда бот будет отправлять сообщения о записи

    /**
     * <b>Users_session</b>
     * Словарь пользователей чья сессия сейчас запущена,
     * ключ - ChatId, значение - User
     */


    //Данные клавиатур:
    String[] defaultKeyBoard = new String[]{"О нас", "Получить консультацию"};
    String[] facultets = new String[]{"ИУ", "СМ", "РК", "МТ", "РЛ", "ИБМ", "ПС", "АК", "РТ", "ФН", "Э", "ЮР", "К", "ЛТ", "СГН", "РКТ", "БМТ"}; // список факультетов
    String[] callsBacksFacultets = getCallsBackDatas(facultets); // см нижу метод getCallsBackDatas()


    public TelegramBot(BotConfig config) {
        this.config = config;
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
        var message = update.getMessage();
        if (update.hasMessage()) {
            long chatId = message.getChatId();
            if (message.hasText()) {
                String messageText = message.getText();
                deleteMessage(update.getMessage());
                switch (messageText) {
                    case "/start":
                    case "/info":
                        send(BotMessageService.CreateMessage(chatId, "Я Бот Учебно-методической комиссии"));

                        break;
                    case "Назад":

                        break;

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


