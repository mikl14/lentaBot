package com.telegrambot.lentaBot.bot.service;


import com.telegrambot.lentaBot.bot.entity.Channel;
import com.telegrambot.lentaBot.bot.entity.Chat;
import com.telegrambot.lentaBot.bot.repository.ChatRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {
    final ChatRepository chatRepository;

    public ChatService(@Lazy ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public void saveChat(Chat chat) {
        chatRepository.save(chat);
    }

    public void deleteChat(Chat chat) {
        chatRepository.delete(chat);
    }

    public Chat findChat(long chatId) {
        return chatRepository.findByChatId(chatId).orElse(null);
    }

    public List<Chat> findChatByChannelId(long channelId) {
        return chatRepository.findChatByChannelId(channelId);
    }

    public List<Chat> getAllChats() {
        return chatRepository.findAll();
    }
}
