package com.telegrambot.lentaBot.bot.service;

import com.telegrambot.lentaBot.bot.config.BotConfig;
import com.telegrambot.lentaBot.bot.entity.Channel;
import com.telegrambot.lentaBot.bot.entity.Chat;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    final BotConfig config;
    final ChatService chatService;
    final ChannelService channelService;
    final RestService restService;
    Logger logger = Logger.getLogger(TelegramBot.class.getName());

    public TelegramBot(BotConfig config, ChatService chatService, RestService restService, ChannelService channelService) {
        this.config = config;
        this.chatService = chatService;
        this.restService = restService;
        this.channelService = channelService;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    /**
     * <b>onUpdateReceived</b> - вызывается автоматически в этом типе бота
     *
     * @param update
     */

    @Override
    public void onUpdateReceived(Update update) {
        //   ниже обработка кнопок с клавиатуры в панели и команд в сообщении!
        if (update.hasMessage()) {  // проверяем есть ли сообщение
            var message = update.getMessage();
            logger.info("New message, from chat: " + message.getChatId());
            Chat chat = chatService.findChat(message.getChatId());
            if (chat == null) {
                chat = new Chat(message.getChatId(), new ArrayList<>());
                chatService.saveChat(chat);
            }

            long chatId = message.getChatId();
            if (chatId != config.getApiChatId()) { // проверяем что чат из которого пришло сообщение это не чат сервиса
                if (message.hasText()) {
                    String messageText = message.getText();
                    deleteMessage(update.getMessage());

                    if (messageText.startsWith("@") || messageText.startsWith("https://t.me/")) { // если сообщения начались в такого формата значит это ссылки на группы
                        if (messageText.startsWith("https://t.me/")) {
                            messageText = messageText.replace("https://t.me/", "@");
                        }
                        String[] response = restService.sendJoinRequest(messageText);
                        if (response == null) {
                            send(BotMessageService.CreateMessage(chatId, "Такой канал не найден, уточните правильность ссылки"));
                        } else {
                            Channel channel = channelService.findChannel(Long.parseLong(response[1]));
                            if (channel == null) {
                                chat.addChannel(new Channel(Long.parseLong(response[1]), response[0], List.of(chat)));

                            } else {
                                channel.addChat(chat);
                                chat.addChannel(channel);
                            }
                            chatService.saveChat(chat);
                            send(BotMessageService.CreateMessage(chatId, "Ваша подписка на канал " + response[0] + " оформлена"));
                        }

                    }

                    switch (messageText) { // обработка команд в текстовых сообщениях
                        case "/start":
                            send(BotMessageService.CreateMessage(chatId, "Я Лента бот, сделаю из любого вашего чата ленту новостей! '\n' Добавляйте меня в чаты в качестве администратора, чтобы сделать из него ленту новостей '\n' Добавляйте своих друзей в свои ленты или делайте тематические ленты"));
                            send(BotMessageService.createInlineKeyBoardMessage(chatId, new String[]{"Инфо", "Мои Подписки"}, new String[]{"info", "subs"}, "Меню бота:", 2));
                            break;
                        case "/info":
                            send(BotMessageService.CreateMessage(chatId, "Инструкция: \n\n 1) Добавьте меня в любой чат \n\n 2) Назначьте администратором, чтобы я мог читать и отправлять сообщения \n\n 3) Отправьте ссылку на любой публичный канал \n\n 4) Наслаждайтесь доступу ко всем постам сразу в одном чате!"));
                            break;
                        case "/menu":
                            send(BotMessageService.createInlineKeyBoardMessage(chatId, new String[]{"Инфо", "Мои Подписки"}, new String[]{"info", "subs"}, "Меню бота:", 2));
                            break;
                        case "/subs":
                            StringBuilder sb = new StringBuilder();
                            int count = 0;
                            for (Channel channel : chat.getChanelList()) {
                                sb.append('\n');
                                sb.append(++count).append(") ");
                                sb.append(channel.getTitle());
                            }

                            send(BotMessageService.CreateMessage(chatId, "Ваши подписки:\n" + sb.toString() + " \n\n Для отписки напишите команду /unsub и номер канала в списке \n Пример: /unsub 1"));
                            break;
                        default:
                            if (messageText.startsWith("/unsub")) { // это команда нужна для отписки и имеет после себя номер канала
                                if (messageText.split(" ").length == 2) {
                                    int index = Integer.parseInt(messageText.split(" ")[1]) - 1;
                                    Channel channel = chat.getChanelList().get(index);

                                    channel.removeChat(chat);
                                    if (channel.getChats().isEmpty()) {
                                        channelService.DeleteChannel(channel);
                                    }
                                    chat.deleteChannel(channel.getChatId());

                                    chatService.saveChat(chat);
                                    send(BotMessageService.CreateMessage(chatId, "Вы отписались от канала " + channel.getTitle()));
                                } else {
                                    send(BotMessageService.CreateMessage(chatId, "Чтобы отписаться используйте команду в формате \"/unsub 1\" "));
                                }
                            }
                    }
                }
            } else { // сюда попадаем в случае если сообщение из чата сервиса
                Channel channel = channelService.findChannel(update.getMessage().getForwardFromChat().getId());
                logger.info("New post from channel : " + channel.getTitle() + " chatId :" + channel.getChatId());
                ForwardMessage forwardMessage = new ForwardMessage();

                if (channel.getChats().isEmpty()) {
                    logger.warning("No subscribers in channel: " + channel.getTitle() + " chatId :" + channel.getChatId());
                }
                for (Chat channelChat : channel.getChats()) {
                    forwardMessage.setChatId(channelChat.getChatId());
                    forwardMessage.setFromChatId(update.getMessage().getChatId());
                    forwardMessage.setMessageId(update.getMessage().getMessageId());
                    send(forwardMessage);
                }


            }

        }
        //   ниже обработка кнопок с клавиатур в сообщении!
        else if (update.hasCallbackQuery()) {
            String query = update.getCallbackQuery().getData();
            long ChatId = update.getCallbackQuery().getMessage().getChatId();
            Chat chat = chatService.findChat(ChatId);
            switch (query) {
                case "info":
                    send(BotMessageService.CreateMessage(ChatId, "Инструкция: \n\n 1) Добавьте меня в любой чат \n\n 2) Назначьте администратором, чтобы я мог читать и отправлять сообщения \n\n 3) Отправьте ссылку на любой публичный канал или ссылку приглашение в закрытый \n\n 4) Наслаждайтесь доступу ко всем постам сразу в одном чате!"));
                    break;

                case "subs":
                    StringBuilder sb = new StringBuilder();
                    int count = 0;
                    for (Channel channel : chat.getChanelList()) {
                        sb.append('\n');
                        sb.append(++count).append(") ");
                        sb.append(channel.getTitle());
                    }

                    send(BotMessageService.CreateMessage(ChatId, "Ваши подписки:\n" + sb.toString() + " \n\n Для отписки напишите команду /unsub и номер канала в списке \n Пример: /unsub 1"));

                    break;
            }

        }
    }


    public void deleteMessage(Message mes) {
        DeleteMessage delMes = new DeleteMessage();
        delMes.setChatId(String.valueOf(mes.getChatId()));
        delMes.setMessageId(mes.getMessageId());
        send(delMes);
    }

    /**
     * <b>send</b>
     * -отправляет в чат пересланное сообщение
     *
     * @param messageObj
     */
    public void send(ForwardMessage messageObj) {
        try {
            execute(messageObj);
        } catch (TelegramApiException ex) {
            throw new RuntimeException(ex);
        }
    }


    /**
     * <b>send</b>
     * -отправляет в чат созданное сообщение с файлом
     *
     * @param messageObj
     */
    public void send(SendMessage messageObj) {
        try {
            execute(messageObj);
        } catch (TelegramApiException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * <b>send</b>
     * - реализует удаление заданного в обьекте сообщения
     *
     * @param messageObj
     */
    public void send(DeleteMessage messageObj) {
        try {
            execute(messageObj);
        } catch (TelegramApiException ex) {
            throw new RuntimeException(ex);
        }
    }
}


