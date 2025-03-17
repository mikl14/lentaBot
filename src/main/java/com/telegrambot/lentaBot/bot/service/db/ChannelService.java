package com.telegrambot.lentaBot.bot.service.db;

import com.telegrambot.lentaBot.bot.entity.Channel;
import com.telegrambot.lentaBot.bot.repository.ChannelRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelService {
    final ChannelRepository channelRepository;

    public ChannelService(@Lazy ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    public void saveChannel(Channel channel) {
        channelRepository.save(channel);
    }

    public void DeleteChannel(Channel channel) {
        channelRepository.delete(channel);
    }

    public Channel findChannel(long chatId) {
        return channelRepository.findByChatId(chatId).orElse(null);
    }

    public Channel findChannelByInviteLink(String inviteLink) {return channelRepository.findByInviteLink(inviteLink).orElse(null);}

    public List<Channel> getAllChannels() {
        return channelRepository.findAll();
    }
}
