package com.telegrambot.lentaBot.bot.service;

import com.telegrambot.lentaBot.bot.config.BotConfig;
import com.telegrambot.lentaBot.bot.entity.Channel;
import com.telegrambot.lentaBot.bot.entity.Chat;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    final BotConfig config;
    final ChatService chatService;
    final ChannelService channelService;
    final RestService restService;
    Logger logger = Logger.getLogger(TelegramBot.class.getName());

    Map<Long, List<Message>> messageMap = new HashMap<>();

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
                                sb.append("<a href=\"" + channel.getInviteLink() + "\">");
                                sb.append(channel.getTitle() + "</a>");
                            }

                            send(BotMessageService.CreateMessage(chatId, "Ваши подписки:\n" + sb.toString() + " \n\n Для отписки напишите команду /unsub и номер канала в списке \n Пример: /unsub 1", "HTML"));
                            break;

                        default:
                            if (update.getMessage().getChatId().equals(config.getAdminChatId()) && messageText.startsWith("/alert")) {
                                List<Chat> chats = chatService.getAllChats();
                                logger.info("Alert in all channels !");

                                SendMessage sendMessage = new SendMessage();

                                for (Chat currentChat : chats) {
                                    sendMessage.setChatId(currentChat.getChatId());
                                    sendMessage.setText(messageText.replace("/alert", ""));
                                    send(sendMessage);
                                }
                            }

                            if (messageText.startsWith("/unsub")) { // это команда нужна для отписки и имеет после себя номер канала
                                if (messageText.split(" ").length == 2) {

                                    Channel channel = null;
                                    try {
                                        int index = Integer.parseInt(messageText.split(" ")[1]) - 1;
                                        if (index > 0) {
                                            channel = chat.getChanelList().get(index);
                                        }

                                    } catch (NumberFormatException e) {
                                        String link = messageText.split(" ")[1];
                                        channel = chat.getChanelList().stream().filter(a -> a.getInviteLink().equals(link)).findFirst().orElse(null);
                                    }
                                    if (channel == null) {
                                        send(BotMessageService.CreateMessage(chatId, "Чтобы отписаться используйте команду в формате \"/unsub 1\" или \"/unsub link\" "));
                                    } else {
                                        removeChannelAndChat(channel, chat);

                                        if (channel.getChats().isEmpty()) {
                                            channelService.DeleteChannel(channel);
                                            restService.sendLeaveRequest(channel.getChatId());
                                        }
                                        send(BotMessageService.CreateMessage(chatId, "Вы отписались от канала " + channel.getTitle()));
                                    }
                                } else {
                                    send(BotMessageService.CreateMessage(chatId, "Чтобы отписаться используйте команду в формате \"/unsub 1\" или \"/unsub link\" "));
                                }
                            }

                            if (messageText.startsWith("/private_sub")) {
                                if (messageText.split(" ").length == 2) {

                                    String link = messageText.split(" ")[1];
                                    Channel channel = channelService.findChannelByInviteLink(link);
                                    if (channel != null) {
                                        saveChannelAndChat(channel, chat);
                                        send(BotMessageService.CreateMessage(chatId, "Ваша подписка на канал " + channel.getTitle() + " оформлена"));
                                    } else {
                                        String[] response = restService.sendPrivateJoinRequest(link);
                                        if (response == null) {
                                            send(BotMessageService.CreateMessage(chatId, "Такой канал не найден, уточните правильность ссылки для подписки на публичный канал используйте \" /sub channelLink\""));
                                        } else {
                                            channel = new Channel(Long.parseLong(response[1]), response[0], response[2], List.of(chat));
                                            saveChannelAndChat(channel, chat);
                                            send(BotMessageService.CreateMessage(chatId, "Ваша подписка на канал " + response[0] + " оформлена"));
                                        }
                                    }
                                } else {
                                    send(BotMessageService.CreateMessage(chatId, "Чтобы подписаться на приватный канал используйте команду в формате \"/private_sub invite_link\" "));
                                }
                            }

                            if (messageText.startsWith("/sub")) {
                                if (messageText.split(" ").length == 2) {

                                    if (messageText.startsWith("@")) {
                                        messageText = messageText.replace("@", "https://t.me/");
                                    }
                                    String link = messageText.split(" ")[1];
                                    Channel channel = channelService.findChannelByInviteLink(link);

                                    if (channel != null) {
                                        saveChannelAndChat(channel, chat);
                                        send(BotMessageService.CreateMessage(chatId, "Ваша подписка на канал " + channel.getTitle() + " оформлена"));
                                    } else {
                                        String[] response = restService.sendJoinRequest(link);
                                        if (response == null) {
                                            send(BotMessageService.CreateMessage(chatId, "Такой канал не найден, уточните правильность ссылки, для подписки на приватный канал используйте \" /private_sub invite_link\""));
                                        } else {
                                            channel = new Channel(Long.parseLong(response[1]), response[0], link, List.of(chat));
                                            saveChannelAndChat(channel, chat);
                                            send(BotMessageService.CreateMessage(chatId, "Ваша подписка на канал " + response[0] + " оформлена"));
                                        }
                                    }

                                } else {
                                    send(BotMessageService.CreateMessage(chatId, "Чтобы подписаться на открытый канал используйте команду в формате \"/sub link\" "));
                                }
                            }

                    }
                }
            } else { // сюда попадаем в случае если сообщение из чата сервиса
                try {
                    Channel channel = channelService.findChannel(update.getMessage().getForwardFromChat().getId());
                    logger.info("New post from channel : " + channel.getTitle() + " chatId :" + channel.getChatId());
                    long mediaId = 0;
                    if (message.getMediaGroupId() != null) {
                        mediaId = Long.parseLong(message.getMediaGroupId());
                    }


                    if (mediaId != 0) {
                        if (messageMap.containsKey(mediaId)) {
                            try {
                                List<Message> messageList = messageMap.get(mediaId);
                                messageList.add(message);
                            } catch (Exception e) {
                                logger.warning("Error on grouping MessageGroup!");
                            }

                        } else {
                            List<Message> messageList = new ArrayList<>();
                            messageList.add(message);
                            messageMap.put(mediaId, messageList);
                        }
                    } else {
                        sendInChats(channel, update.getMessage());
                    }

                } catch (Exception e) {
                    logger.info("Channel not found in base: !");
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
                        sb.append("<a href=\"" + channel.getInviteLink() + "\">");
                        sb.append(channel.getTitle() + "</a>");
                    }

                    send(BotMessageService.CreateMessage(ChatId, "Ваши подписки:\n" + sb.toString() + " \n\n Для отписки напишите команду /unsub и номер канала в списке \n Пример: /unsub 1", "HTML"));
                    break;
            }

        }
    }

    /**
     * <b>sendMediaGroups</b>
     * каждые 5 секунд отправляет накопленные медиа коллекции и отчищает список
     */
    @Scheduled(fixedRate = 5000)
    public void sendMediaGroups() {
        if (!messageMap.isEmpty()) {
            for (long key : messageMap.keySet()) {
                Channel channel = channelService.findChannel(messageMap.get(key).get(0).getForwardFromChat().getId());


                List<InputMedia> mediaPhotos = new ArrayList<>();
                for (Message mes : messageMap.get(key)) {
                    if (mes.hasPhoto()) {
                        mediaPhotos.add(new InputMediaPhoto(mes.getPhoto().get(0).getFileId()));
                    }
                    if (mes.hasVideo()) {
                        mediaPhotos.add(new InputMediaVideo(mes.getVideo().getFileId()));
                    }
                    if (mes.hasAudio()) {
                        mediaPhotos.add(new InputMediaAudio(mes.getAudio().getFileId()));
                    }
                    if (mes.hasAnimation()) {
                        mediaPhotos.add(new InputMediaAnimation(mes.getAnimation().getFileId()));
                    }

                }

                mediaPhotos.get(0).setCaption(messageMap.get(key).get(0).getCaption());
                // Отправляем медиагруппу (если нужно отправить несколько фотографий)
                sendInChats(channel, mediaPhotos);

            }
            messageMap.clear();
        }
    }

    public void sendInChats(Channel channel, Message message) {
        ForwardMessage forwardMessage = new ForwardMessage();

        if (channel.getChats().isEmpty()) {
            logger.warning("No subscribers in channel: " + channel.getTitle() + " chatId :" + channel.getChatId());
        }
        for (Chat channelChat : channel.getChats()) {
            forwardMessage.setChatId(channelChat.getChatId());
            forwardMessage.setFromChatId(message.getChatId());
            forwardMessage.setMessageId(message.getMessageId());
            forwardMessage.setMessageThreadId(message.getMessageThreadId());

            send(forwardMessage);
        }
    }

    public void sendInChats(Channel channel, List<InputMedia> mediaPhotos) {
        SendMediaGroup sendMediaGroup = new SendMediaGroup();

        if (channel.getChats().isEmpty()) {
            logger.warning("No subscribers in channel: " + channel.getTitle() + " chatId :" + channel.getChatId());
        }
        for (Chat channelChat : channel.getChats()) {
            sendMediaGroup.setChatId(String.valueOf(channelChat.getChatId()));
            sendMediaGroup.setMedias(mediaPhotos);
            send(sendMediaGroup);
        }
    }

    private void send(SendMediaGroup sendMediaGroup) {
        try {
            execute(sendMediaGroup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void saveChannelAndChat(Channel channel, Chat chat) {
        channel.addChat(chat);
        channelService.saveChannel(channel);
        chat.addChannel(channel);
        chatService.saveChat(chat);
    }

    public void removeChannelAndChat(Channel channel, Chat chat) {
        channel.removeChat(chat);
        channelService.saveChannel(channel);
        chat.deleteChannel(channel.getChatId());
        chatService.saveChat(chat);
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


