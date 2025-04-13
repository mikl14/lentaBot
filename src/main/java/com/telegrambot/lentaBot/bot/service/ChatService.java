package com.telegrambot.lentaBot.bot.service;

import com.telegrambot.lentaBot.bot.states.ChatEvents;
import com.telegrambot.lentaBot.bot.states.ChatStates;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final StateMachineFactory<ChatStates, ChatEvents> stateMachineFactory;

    private final StateMachinePersister<ChatStates, ChatEvents, String> persister;

    public ChatService(StateMachineFactory<ChatStates, ChatEvents> stateMachineFactory, StateMachinePersister<ChatStates, ChatEvents, String> persister) {
        this.stateMachineFactory = stateMachineFactory;
        this.persister = persister;
    }

    public void subscribe (String chatId)
    {
        StateMachine<ChatStates, ChatEvents> stateMachine = stateMachineFactory.getStateMachine();
        // Восстановление состояния из Redis
        try {
            persister.restore(stateMachine, chatId);
        } catch (Exception e) {
            System.out.println("Ошибка восстановления: " + e.getMessage());
        }

        // Отправка события
        stateMachine.sendEvent(ChatEvents.SUBSCRIBE);

        try {
            persister.persist(stateMachine, chatId);
            System.out.println("Текущее состояние: " + stateMachine.getState().getId()); // Для отладки
        } catch (Exception e) {
            System.out.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    public void deactivate(String chatId)  {
        StateMachine<ChatStates, ChatEvents> stateMachine = stateMachineFactory.getStateMachine();
        // Восстановление состояния из Redis
        try {
            persister.restore(stateMachine, chatId);
        } catch (Exception e) {
            System.out.println("Ошибка восстановления: " + e.getMessage());
        }

        // Отправка события
        stateMachine.sendEvent(ChatEvents.DEACTIVATE);

        try {
            persister.persist(stateMachine, chatId);
            System.out.println("Текущее состояние: " + stateMachine.getState().getId()); // Для отладки
        } catch (Exception e) {
            System.out.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    public ChatStates getChatState(String chatId) {
        StateMachine<ChatStates, ChatEvents> stateMachine = stateMachineFactory.getStateMachine();

        // Восстановление состояния из Redis


        try {
            persister.restore(stateMachine, chatId);
            // Возвращение текущего состояния
            return stateMachine.getState().getId();
        } catch (Exception e) {
            // Обработка исключения
            System.out.println("No session"+ e);
            return null;
        }
    }
}
