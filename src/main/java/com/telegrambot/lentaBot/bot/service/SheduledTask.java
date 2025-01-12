package com.telegrambot.lentaBot.bot.service;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@EnableScheduling
@Component
public class SheduledTask {

    private TelegramBot telegramBot;

    public SheduledTask(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @Scheduled(fixedRate = 1000*60) // 3600000 мс = 1 час
    public void sendHourlyMessage() {
/*    *//*    SendMessage message = new SendMessage();
        message.setChatId(-1002319393193l); // Укажите ID канала или чата
        message.setText("hi");
*//*
        try {
            telegramBot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }*/
    }
}
