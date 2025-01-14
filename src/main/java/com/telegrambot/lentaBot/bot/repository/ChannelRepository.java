package com.telegrambot.lentaBot.bot.repository;

import com.telegrambot.lentaBot.bot.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {
    List<Channel> findById(Integer id);

    Optional<Channel> findByChatId(Long chatId);

    Optional<Channel> findByInviteLink(String inviteLink);
}
