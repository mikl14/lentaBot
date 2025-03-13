package com.telegrambot.lentaBot.bot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;

public class QueriesResultAgregator {

    public static boolean isOK(HttpStatus status)
    {
        return status.equals(HttpStatus.OK);
    }
}
