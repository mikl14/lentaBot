package com.telegrambot.lentaBot.bot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.springframework.context.annotation.Lazy;

import java.util.List;

@Entity
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    private Long chatId;

    @Setter
    @Getter
    private String title;


    @Setter
    @Getter
    @ManyToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    @JoinTable(
            name = "chat_channel",
            joinColumns = @JoinColumn(name = "channel_id"),
            inverseJoinColumns = @JoinColumn(name = "chat_id")
    )
    private List<Chat> chats;

    public Channel() {
    }

    public Channel(Long chatId, String title, List<Chat> chats) {
        this.chatId = chatId;
        this.title = title;
        this.chats = chats;
    }

    public void addChat(Chat chat)
    {
        if(chats.stream().noneMatch(x -> x.getChatId().equals(chat.getChatId()))) {
            chats.add(chat);
        }
    }
}
