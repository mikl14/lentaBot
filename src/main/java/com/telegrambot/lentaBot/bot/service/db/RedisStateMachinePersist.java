package com.telegrambot.lentaBot.bot.service.db;

import com.telegrambot.lentaBot.bot.states.ChatEvents;
import com.telegrambot.lentaBot.bot.states.ChatStates;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.support.DefaultStateMachineContext;

import java.util.concurrent.TimeUnit;

public class RedisStateMachinePersist implements StateMachinePersist<ChatStates, ChatEvents, String> {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisStateMachinePersist(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void write(StateMachineContext<ChatStates, ChatEvents> context, String contextObj) throws Exception {
        String stateId = context.getState().toString();
        redisTemplate.opsForValue().set(contextObj, stateId);
        redisTemplate.expire(contextObj, 5, TimeUnit.MINUTES);
    }

    @Override
    public StateMachineContext<ChatStates, ChatEvents> read(String contextObj) throws Exception {
        String stateId = redisTemplate.opsForValue().get(contextObj);
        if (stateId != null) {
            return new DefaultStateMachineContext<>(ChatStates.valueOf(stateId), null, null, null, null);
        }
        return null;
    }
}
