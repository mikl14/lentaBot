package com.telegrambot.lentaBot.bot.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class Chat {

    private Long id;

    @Setter
    @Getter
    private Long chatId;

    @Setter
    @Getter
    private List<Channel> chanelList;

    public Chat() {
    }

    public Chat(Long chatId, List<Channel> chanelList) {
        this.chatId = chatId;
        this.chanelList = chanelList;
    }

    public void addChannel(Channel channel) {
        if (chanelList.stream().noneMatch(x -> x.getChatId().equals(channel.getChatId()))) {
            chanelList.add(channel);
        }
    }

    public void deleteChannel(Long channelId) {
        chanelList.removeIf(a -> a.getChatId().equals(channelId));
    }

}
