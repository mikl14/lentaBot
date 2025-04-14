package com.telegrambot.lentaBot.bot.config;

import com.telegrambot.lentaBot.bot.states.ChatEvents;
import com.telegrambot.lentaBot.bot.states.ChatStates;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<ChatStates, ChatEvents> {
    @Override
    public void configure(StateMachineConfigurationConfigurer<ChatStates, ChatEvents> config) throws Exception {
        config.withConfiguration().autoStartup(true);
    }

    @Override
    public void configure(StateMachineStateConfigurer<ChatStates, ChatEvents> states) throws Exception {
        states.withStates()
                .initial(ChatStates.INACTIVE)
                .state(ChatStates.SUBSCRIBED)
                .state(ChatStates.PRIVATE_SUBSCRIBED)
                .state(ChatStates.UNSUBED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<ChatStates, ChatEvents> transitions) throws Exception {
        transitions.withExternal()
                .source(ChatStates.INACTIVE).target(ChatStates.SUBSCRIBED).event(ChatEvents.SUBSCRIBE)
                .and()

                .withExternal()
                .source(ChatStates.SUBSCRIBED).target(ChatStates.INACTIVE).event(ChatEvents.DEACTIVATE)

                .and()
                .withExternal()
                .source(ChatStates.INACTIVE).target(ChatStates.PRIVATE_SUBSCRIBED).event(ChatEvents.PRIVATE_SUBSCRIBE)

                .and()
                .withExternal()
                .source(ChatStates.PRIVATE_SUBSCRIBED).target(ChatStates.INACTIVE).event(ChatEvents.DEACTIVATE)

                .and()
                .withExternal()
                .source(ChatStates.INACTIVE).target(ChatStates.UNSUBED).event(ChatEvents.UNSUB)

                .and()
                .withExternal()
                .source(ChatStates.UNSUBED).target(ChatStates.INACTIVE).event(ChatEvents.DEACTIVATE)
        ;
    }
}
