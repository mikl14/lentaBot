package com.telegrambot.lentaBot.bot.states;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Service;

@Service
public class StateChatService {
    private final StateMachineFactory<ChatStates, ChatEvents> stateMachineFactory;

    private final StateMachinePersister<ChatStates, ChatEvents, String> persister;

    public StateChatService(StateMachineFactory<ChatStates, ChatEvents> stateMachineFactory, StateMachinePersister<ChatStates, ChatEvents, String> persister) {
        this.stateMachineFactory = stateMachineFactory;
        this.persister = persister;
    }

    private void changeState(String chatId, ChatEvents chatEvents) {
        StateMachine<ChatStates, ChatEvents> stateMachine = stateMachineFactory.getStateMachine();
        // Восстановление состояния из Redis
        try {
            persister.restore(stateMachine, chatId);
        } catch (Exception e) {
            System.out.println("Ошибка восстановления: " + e.getMessage());
        }

        // Отправка события
        stateMachine.sendEvent(chatEvents);

        try {
            persister.persist(stateMachine, chatId);
            System.out.println("Текущее состояние: " + stateMachine.getState().getId()); // Для отладки
        } catch (Exception e) {
            System.out.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    public void subscribe(String chatId) {
        deactivate(chatId);
        changeState(chatId, ChatEvents.SUBSCRIBE);
    }

    public void privateSubscribe(String chatId) {
        deactivate(chatId);
        changeState(chatId, ChatEvents.PRIVATE_SUBSCRIBE);
    }

    public void unSub(String chatId) {
        deactivate(chatId);
        changeState(chatId, ChatEvents.UNSUB);
    }


    public void deactivate(String chatId) {
        changeState(chatId, ChatEvents.DEACTIVATE);
    }

    public ChatStates getChatState(String chatId) {
        StateMachine<ChatStates, ChatEvents> stateMachine = stateMachineFactory.getStateMachine();
        try {
            persister.restore(stateMachine, chatId);
            // Возвращение текущего состояния
            return stateMachine.getState().getId();
        } catch (Exception e) {
            // Обработка исключения
            System.out.println("No session" + e);
            return null;
        }
    }
}
