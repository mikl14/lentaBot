package com.telegrambot.lentaBot.bot.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    @KafkaListener(topics = "telegramContent")
    public void listen(ConsumerRecord<String,String> message)
    {
        System.out.println("message: " + message.value());
    }
}
