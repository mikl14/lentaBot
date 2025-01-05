package com.telegrambot.lentaBot.config;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import com.telegrambot.lentaBot.service.TelegramBot;

@Component
public class BotInit
{

    TelegramBot bot;

    public BotInit(TelegramBot bot)
    {
        this.bot = bot;
    }


    @EventListener({ContextRefreshedEvent.class})
    public void Init() throws TelegramApiException
    {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(bot);
        }
        catch (TelegramApiException e)
        {
            System.out.println(e);
        }

    }
}
