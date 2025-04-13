package com.telegrambot.lentaBot.bot.service;

import com.telegrambot.lentaBot.bot.config.BotConfig;
import com.telegrambot.lentaBot.bot.entity.Channel;
import com.telegrambot.lentaBot.bot.entity.Chat;
import com.telegrambot.lentaBot.bot.service.db.DataBaseRestService;
import com.telegrambot.lentaBot.bot.service.message.BotMessageService;
import com.telegrambot.lentaBot.bot.service.message.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.media.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    final BotConfig config;

    final RestService restService;

    final DataBaseRestService dataBaseRestService;

    final MessageBuilder messageBuilder;
    Logger logger = Logger.getLogger(TelegramBot.class.getName());

    Map<Long, List<Message>> messageMap = new HashMap<>();

    public TelegramBot(BotConfig config, RestService restService, MessageBuilder messageBuilder, DataBaseRestService dataBaseRestService) {
        this.config = config;
        this.restService = restService;
        this.messageBuilder = messageBuilder;
        this.dataBaseRestService = dataBaseRestService;
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

            long chatId = message.getChatId();
            if (chatId != config.getApiChatId()) { // проверяем что чат из которого пришло сообщение это не чат сервиса

                logger.info("New message, from chat: " + message.getChatId());
                Chat chat = dataBaseRestService.getChatByChatId(message.getChatId());
                if (chat == null) {
                    chat = new Chat(message.getChatId(), new ArrayList<>());
                    dataBaseRestService.saveChat(chat);
                }


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
                            send(BotMessageService.createInlineKeyBoardMessage(chatId, new String[]{"Инфо", "Мои Подписки", "Топ 5 каналов"}, new String[]{"info", "subs", "rating"}, "Меню бота:", 2));
                            break;
                        case "/subs":
                            StringBuilder sb = new StringBuilder();
                            int count = 0;

                            List<Channel> channelList = dataBaseRestService.getChatChannelsByChatId(chat.getChatId());
                            for (Channel channel : channelList) {
                                sb.append('\n');
                                sb.append(++count).append(") ");
                                sb.append("<a href=\"" + channel.getInviteLink() + "\">");
                                sb.append(channel.getTitle() + "</a>");
                            }

                            send(BotMessageService.CreateMessage(chatId, "Ваши подписки:\n" + sb.toString() + " \n\n Для отписки напишите команду /unsub и номер канала в списке \n Пример: /unsub 1", "HTML"));
                            break;

                        default:
                            if (update.getMessage().getChatId().equals(config.getAdminChatId()) && messageText.startsWith("/alert")) {
                                List<Long> chatsIds = dataBaseRestService.getAllChatIdOfChats();
                                logger.info("Alert in all channels !");

                                SendMessage sendMessage = new SendMessage();

                                for (Long currentChatId : chatsIds) {
                                    sendMessage.setChatId(currentChatId);
                                    sendMessage.setText(messageText.replace("/alert", ""));
                                    send(sendMessage);
                                }
                            }

                            if (messageText.startsWith("/unsub")) { // это команда нужна для отписки и имеет после себя номер канала
                                if (messageText.split(" ").length == 2) {

                                    Channel channel = null;
                                    List<Channel> channels = dataBaseRestService.getChatChannelsByChatId(chat.getChatId());
                                    try {
                                        int index = Integer.parseInt(messageText.split(" ")[1]) - 1;
                                        if (index > 0) {
                                            channel = channels.get(index);
                                        }

                                    } catch (NumberFormatException e) {
                                        String link = messageText.split(" ")[1];
                                        channel = channels.stream().filter(a -> a.getInviteLink().equals(link)).findFirst().orElse(null);
                                    }
                                    if (channel == null) {
                                        send(BotMessageService.CreateMessage(chatId, "Чтобы отписаться используйте команду в формате \"/unsub 1\" или \"/unsub link\" "));
                                    } else {

                                        dataBaseRestService.removeChannelFromChat(chat, channel);

                                        if (dataBaseRestService.getChannelChatsByChatId(channel.getChatId()).isEmpty()) {

                                            dataBaseRestService.deleteChannel(channel.getChatId());
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
                                    Channel channel = dataBaseRestService.getChannelByInviteLink(link);
                                    if (channel != null) {
                                        dataBaseRestService.addChannelInChatByChatId(chat, channel);
                                        send(BotMessageService.CreateMessage(chatId, "Ваша подписка на канал " + channel.getTitle() + " оформлена"));
                                    } else {
                                        String[] response = restService.sendPrivateJoinRequest(link);
                                        if (response == null) {
                                            send(BotMessageService.CreateMessage(chatId, "Такой канал не найден, уточните правильность ссылки для подписки на публичный канал используйте \" /sub channelLink\""));
                                        } else {
                                            channel = new Channel(Long.parseLong(response[1]), response[0], response[2], List.of(chat));
                                            dataBaseRestService.addChannelInChatByChatId(chat, channel);
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
                                    Channel channel = dataBaseRestService.getChannelByInviteLink(link);

                                    if (channel != null) {
                                        dataBaseRestService.addChannelInChatByChatId(chat, channel);
                                        send(BotMessageService.CreateMessage(chatId, "Ваша подписка на канал " + channel.getTitle() + " оформлена"));
                                    } else {
                                        String[] response = restService.sendJoinRequest(link);
                                        if (response == null) {
                                            send(BotMessageService.CreateMessage(chatId, "Такой канал не найден, уточните правильность ссылки, для подписки на приватный канал используйте \" /private_sub invite_link\""));
                                        } else {
                                            channel = new Channel(Long.parseLong(response[1]), response[0], link, List.of(chat));
                                            dataBaseRestService.addChannelInChatByChatId(chat, channel);
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
                    Channel channel = dataBaseRestService.getChannelByChatId(update.getMessage().getForwardFromChat().getId());
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
                    logger.info("Channel not found in base: !" + e);
                }
            }

        }
        //   ниже обработка кнопок с клавиатур в сообщении!
        else if (update.hasCallbackQuery()) {
            String query = update.getCallbackQuery().getData();
            long ChatId = update.getCallbackQuery().getMessage().getChatId();
            Chat chat = dataBaseRestService.getChatByChatId(ChatId);
            StringBuilder sb = new StringBuilder();
            int count = 0;
            switch (query) {
                case "info":
                    send(BotMessageService.CreateMessage(ChatId, "Инструкция: \n\n 1) Добавьте меня в любой чат \n\n 2) Назначьте администратором, чтобы я мог читать и отправлять сообщения \n\n 3) Отправьте ссылку на любой публичный канал или ссылку приглашение в закрытый \n\n 4) Наслаждайтесь доступу ко всем постам сразу в одном чате!"));
                    break;

                case "subs":
                    sb = new StringBuilder();
                    count = 0;
                    for (Channel channel : chat.getChanelList()) {
                        sb.append('\n');
                        sb.append(++count).append(") ");
                        sb.append("<a href=\"" + channel.getInviteLink() + "\">");
                        sb.append(channel.getTitle() + "</a>");
                    }

                    send(BotMessageService.CreateMessage(ChatId, "Ваши подписки:\n" + sb.toString() + " \n\n Для отписки напишите команду /unsub и номер канала в списке \n Пример: /unsub 1", "HTML"));
                    break;

                case "rating":
                    sb = new StringBuilder();
                    List<Channel> channels = dataBaseRestService.getChannelRating();

                    for (Channel channel : channels) {
                        sb.append('\n');
                        sb.append("<a href=\"" + channel.getInviteLink() + "\">");
                        sb.append(channel.getTitle() + "</a>");
                    }

                    send(BotMessageService.CreateMessage(ChatId, "Топ 5 каналов:\n" + sb.toString(), "HTML"));
                    break;
            }

        }
    }

    /**
     * <b>isBotInGroup</b>
     * проверяет статус бота в группе
     */
    public boolean isBotInGroup(String chatId) {
        GetChatMember getChatMember = new GetChatMember();
        try {
            getChatMember.setChatId(chatId);
            getChatMember.setUserId(getMe().getId()); // Получаем ID бота
            ChatMember chatMember = execute(getChatMember);
            // Проверяем статус бота в группе
            String status = chatMember.getStatus();
            return !status.equals("kicked") && !status.equals("left");
        } catch (TelegramApiException e) {
            if (e.getMessage().contains("403")) {
                System.out.println("Бот был кикнут из группы.");
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * <b>sendMediaGroups</b>
     * каждые 5 секунд отправляет накопленные медиа коллекции и отчищает список
     */
    @Scheduled(fixedRate = 5000)
    public void sendMediaGroups() {
        try {
            if (!messageMap.isEmpty()) {
                for (long key : messageMap.keySet()) {
                    Channel channel = dataBaseRestService.getChannelByChatId(messageMap.get(key).get(0).getForwardFromChat().getId());


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

                    if (messageMap.get(key).get(0).getCaption() != null) {
                        mediaPhotos.get(0).setCaption(messageMap.get(key).get(0).getCaption() + "\n#" + messageMap.get(key).get(0).getForwardFromChat().getTitle() + "\n Источник: " + channel.getInviteLink());
                    } else {
                        mediaPhotos.get(0).setCaption("#" + replaceSpecialChars(messageMap.get(key).get(0).getForwardFromChat().getTitle()) + "\n Источник: " + channel.getInviteLink());
                    }
                    // Отправляем медиагруппу (если нужно отправить несколько фотографий)
                    sendInChats(channel, mediaPhotos);

                }
                messageMap.clear();
            }
        } catch (Exception e) {
            messageMap.clear();
            logger.warning("Critical scheduled method error : " + e);
        }

    }

    public void sendInChats(Channel channel, Message message) {

        SendMediaBotMethod<Message> sendMediaBotMethod = messageBuilder.createMessage(message);

        List<Chat> chats = dataBaseRestService.getChannelChatsByChatId(channel.getChatId());

        if (sendMediaBotMethod == null) {
            BotApiMethodMessage botApiMethodMessage = messageBuilder.createApiMessage(message);
            if (chats.isEmpty()) {
                logger.warning("No subscribers in channel: " + channel.getTitle() + " chatId :" + channel.getChatId());
            }
            for (Chat channelChat : chats) {
                try {
                    if (isBotInGroup(String.valueOf(channelChat.getChatId()))) {
                        send(botApiMethodMessage, channelChat.getChatId(), channel);
                    } else {
                        logger.log(Level.WARNING, "Bot was kicked from chat!");
                        dataBaseRestService.deleteChat(channelChat.getChatId());
                    }

                } catch (RuntimeException e) {
                    logger.log(Level.WARNING, "Error on send message!");
                }
            }
        } else {
            if (chats.isEmpty()) {
                logger.warning("No subscribers in channel: " + channel.getTitle() + " chatId :" + channel.getChatId());
            }
            for (Chat channelChat : chats) {
                try {
                    if (isBotInGroup(String.valueOf(channelChat.getChatId()))) {
                        send(sendMediaBotMethod, channelChat.getChatId(), channel);
                    } else {
                        logger.log(Level.WARNING, "Bot was kicked from chat!");
                        dataBaseRestService.deleteChat(channelChat.getChatId());
                    }
                } catch (RuntimeException e) {
                    logger.log(Level.WARNING, "Error on send message!");
                }
            }
        }
    }

    public void sendInChats(Channel channel, List<InputMedia> mediaPhotos) {
        SendMediaGroup sendMediaGroup = new SendMediaGroup();

        List<Chat> chats = dataBaseRestService.getChannelChatsByChatId(channel.getChatId());
        if (chats.isEmpty()) {
            logger.warning("No subscribers in channel: " + channel.getTitle() + " chatId :" + channel.getChatId());
        }
        for (Chat channelChat : chats) {
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


    /**
     * <b>send</b>
     * -отправляет в чат пересланное сообщение
     *
     * @param messageObj
     */
    public void send(SendMediaBotMethod<Message> messageObj, long chatId, Channel channel) {

        StringBuilder caption = new StringBuilder("\n#" + replaceSpecialChars(channel.getTitle()) + "\n Источник: " + channel.getInviteLink());

        switch (messageObj.getClass().getSimpleName()) {

            case "SendPhoto": {
                try {
                    ((SendPhoto) messageObj).setChatId(chatId);

                    if (((SendPhoto) messageObj).getCaption() != null) {
                        if (!((SendPhoto) messageObj).getCaption().endsWith(caption.toString())) {
                            ((SendPhoto) messageObj).setCaption(((SendPhoto) messageObj).getCaption() + caption);
                        }
                    } else {
                        ((SendPhoto) messageObj).setCaption(caption.toString());
                    }

                    ((SendPhoto) messageObj).setParseMode("HTML");
                    execute((SendPhoto) messageObj);
                } catch (TelegramApiException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            }
            case "SendVideo": {
                try {

                    assert messageObj instanceof SendVideo;
                    ((SendVideo) messageObj).setChatId(chatId);
                    if (((SendVideo) messageObj).getCaption() != null) {
                        if (!((SendVideo) messageObj).getCaption().endsWith(caption.toString())) {
                            ((SendVideo) messageObj).setCaption(((SendVideo) messageObj).getCaption() + caption);
                        }
                    } else {
                        ((SendVideo) messageObj).setCaption(caption.toString());
                    }

                    execute((SendVideo) messageObj);
                } catch (TelegramApiException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            }
            case "SendAudio": {
                try {
                    assert messageObj instanceof SendAudio;
                    ((SendAudio) messageObj).setChatId(chatId);

                    if (((SendAudio) messageObj).getCaption() != null) {
                        if (!((SendAudio) messageObj).getCaption().endsWith(caption.toString())) {
                            ((SendAudio) messageObj).setCaption(((SendAudio) messageObj).getCaption() + caption);
                        }
                    } else {
                        ((SendAudio) messageObj).setCaption(caption.toString());
                    }

                    execute((SendAudio) messageObj);
                } catch (TelegramApiException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            }
            case "SendVoice": {
                try {
                    assert messageObj instanceof SendVoice;
                    ((SendVoice) messageObj).setChatId(chatId);
                    if (((SendVoice) messageObj).getCaption() != null) {
                        if (!((SendVoice) messageObj).getCaption().endsWith(caption.toString())) {
                            ((SendVoice) messageObj).setCaption(((SendVoice) messageObj).getCaption() + caption);
                        }
                    } else {
                        ((SendVoice) messageObj).setCaption(caption.toString());
                    }
                    execute((SendVoice) messageObj);
                } catch (TelegramApiException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            }
            case "SendVideoNote": {
                try {
                    assert messageObj instanceof SendVideoNote;
                    ((SendVideoNote) messageObj).setChatId(chatId);
                    execute((SendVideoNote) messageObj);
                } catch (TelegramApiException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            }
            case "SendAnimation": {
                try {
                    assert messageObj instanceof SendAnimation;
                    ((SendAnimation) messageObj).setChatId(chatId);
                    if (((SendAnimation) messageObj).getCaption() != null) {
                        if (!((SendAnimation) messageObj).getCaption().endsWith(caption.toString())) {
                            ((SendAnimation) messageObj).setCaption(((SendAnimation) messageObj).getCaption() + caption);
                        }
                    } else {
                        ((SendAnimation) messageObj).setCaption(caption.toString());
                    }
                    execute((SendAnimation) messageObj);
                } catch (TelegramApiException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            }
            case "SendDocument": {
                try {
                    assert messageObj instanceof SendDocument;
                    ((SendDocument) messageObj).setChatId(chatId);
                    if (((SendDocument) messageObj).getCaption() != null) {
                        if (!((SendDocument) messageObj).getCaption().endsWith(caption.toString())) {
                            ((SendDocument) messageObj).setCaption(((SendDocument) messageObj).getCaption() + caption);
                        }
                    } else {
                        ((SendDocument) messageObj).setCaption(caption.toString());
                    }
                    execute((SendDocument) messageObj);
                } catch (TelegramApiException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            }

            case "SendSticker": {
                try {
                    assert messageObj instanceof SendSticker;
                    ((SendSticker) messageObj).setChatId(chatId);
                    execute((SendSticker) messageObj);
                } catch (TelegramApiException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            }

        }


    }

    /**
     * <b>send</b>
     * -отправляет в чат пересланное сообщение
     *
     * @param messageObj
     */
    public void send(BotApiMethodMessage messageObj, long chatId, Channel channel) {

        StringBuilder caption = new StringBuilder("\n#" + replaceSpecialChars(channel.getTitle()) + "\n Источник: " + channel.getInviteLink());


        switch (messageObj.getClass().getSimpleName()) {

            case "SendMessage": {
                try {
                    ((SendMessage) messageObj).setChatId(chatId);
                    if (!((SendMessage) messageObj).getText().endsWith(caption.toString())) {
                        ((SendMessage) messageObj).setText(((SendMessage) messageObj).getText() + caption);
                    }
                    execute((SendMessage) messageObj);
                } catch (TelegramApiException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            }
            case "ForwardMessage": {
                try {
                    assert messageObj instanceof ForwardMessage;
                    ((ForwardMessage) messageObj).setChatId(chatId);
                    execute((ForwardMessage) messageObj);
                } catch (TelegramApiException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            }
            case "SendLocation": {
                try {
                    assert messageObj instanceof SendLocation;
                    ((SendLocation) messageObj).setChatId(chatId);
                    execute((SendLocation) messageObj);
                } catch (TelegramApiException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            }
        }
    }

    public static String replaceSpecialChars(String str) {
        // Регулярное выражение для поиска любых символов, не являющихся буквами или цифрами
        Pattern pattern = Pattern.compile("[^a-zA-Zа-яА-Я0-9\\s]");

        // Заменяем все найденные символы на подчеркивание
        Matcher matcher = pattern.matcher(str);

        String res = matcher.replaceAll("_");
        if (res.contains(" ")) res = res.replace(" ", "_");
        return res;
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

}


