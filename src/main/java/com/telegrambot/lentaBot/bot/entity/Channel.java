package com.telegrambot.lentaBot.bot.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class Channel {

    private Long id;

    @Setter
    @Getter
    private Long chatId;

    @Setter
    @Getter
    private String title;

    @Setter
    @Getter
    private String inviteLink;

    @Setter
    @Getter
    private List<Chat> chats;

    public Channel() {
    }

    public Channel(Long chatId, String title, List<Chat> chats) {
        this.chatId = chatId;
        this.title = title;
        this.chats = chats;
    }

    public Channel(Long chatId, String title, String inviteLink, List<Chat> chats) {
        this.chatId = chatId;
        this.title = title;
        this.inviteLink = inviteLink;
        this.chats = chats;
    }

    public void addChat(Chat chat) {
        if (chats.stream().noneMatch(x -> x.getChatId().equals(chat.getChatId()))) {
            chats.add(chat);
        }
    }

    public void removeChat(Chat chat) {
        if (chats.stream().anyMatch(x -> x.getChatId().equals(chat.getChatId()))) {
            chats.remove(chat);
        }
    }
}
