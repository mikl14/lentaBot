package com.telegrambot.lentaBot.bot.config;

import com.telegrambot.lentaBot.bot.service.db.RedisStateMachinePersist;
import com.telegrambot.lentaBot.bot.states.ChatEvents;
import com.telegrambot.lentaBot.bot.states.ChatStates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @Bean
    public StateMachinePersist<ChatStates, ChatEvents, String> redisPersist(RedisTemplate<String, String> redisTemplate) {
        return new RedisStateMachinePersist(redisTemplate);
    }

    @Bean
    public StateMachinePersister<ChatStates, ChatEvents, String> persister(StateMachinePersist<ChatStates, ChatEvents, String> persist) {
        return new DefaultStateMachinePersister<>(persist);
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("localhost", 6379);
        config.setDatabase(1);
        return new LettuceConnectionFactory(config);
    }


}