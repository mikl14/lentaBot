package com.telegrambot.lentaBot.bot.service;

import com.telegrambot.lentaBot.bot.entity.Channel;
import com.telegrambot.lentaBot.bot.repository.ChannelRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ChannelService {
    final ChannelRepository channelRepository;

    public ChannelService(@Lazy ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    public void SaveChannel(Channel channel) {
        channelRepository.save(channel);
    }

    public void DeleteChannel(Channel channel) {
        channelRepository.delete(channel);
    }

    public Channel findChannel(long chatId) {
        return channelRepository.findByChatId(chatId).orElse(null);
    }
}
