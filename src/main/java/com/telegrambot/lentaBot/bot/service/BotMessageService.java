package com.telegrambot.lentaBot.bot.service;

import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class BotMessageService {
    public static SendMessage CreateMessage(long chatId, String Text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(Text);
        return message;
    }

    public static ForwardMessage forwardMessage(String toChatId, Long fromChatId, int messageId) {
        var message = new ForwardMessage();
        message.setChatId(toChatId);
        message.setFromChatId(fromChatId.toString());
        message.setMessageId(messageId);
        return message;
    }


    public static SendMessage createKeyboard(long chatId, String[] buttonLabels) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        for (String label : buttonLabels) {
            KeyboardRow row = new KeyboardRow();
            row.add(label);
            keyboard.add(row);
        }
        keyboardMarkup.setKeyboard(keyboard);
        SendMessage message = new SendMessage();
        message.setText("Выберите нужный пункт меню:");
        message.setChatId(String.valueOf(chatId));
        message.setReplyMarkup(keyboardMarkup);
        return message;
    }

    public static InlineKeyboardMarkup createInlineKeyBoard(String[] buttonLabels, String[] callBacksDatas) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> buttonList = new ArrayList<>();
        for (int i = 0; i < buttonLabels.length; i++) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(buttonLabels[i]);
            inlineKeyboardButton.setCallbackData(callBacksDatas[i]);

            buttonList.add(inlineKeyboardButton);

        }


        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();


        int sublistSize = 5;

        for (int i = 0; i < buttonList.size(); i += sublistSize) {
            List<InlineKeyboardButton> sublist = buttonList.subList(i, Math.min(i + sublistSize, buttonList.size()));
            rowList.add(sublist);
        }
        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }

    public static SendMessage createInlineKeyBoardMessage(long chatId, String[] buttonLabels, String[] callBacksDatas, String keyBoardName, int buttonsInRow) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> buttonList = new ArrayList<>();
        for (int i = 0; i < buttonLabels.length; i++) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(buttonLabels[i]);
            inlineKeyboardButton.setCallbackData(callBacksDatas[i]);
            buttonList.add(inlineKeyboardButton);
        }


        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();


        for (int i = 0; i < buttonList.size(); i += buttonsInRow) {
            List<InlineKeyboardButton> sublist = buttonList.subList(i, Math.min(i + buttonsInRow, buttonList.size()));
            rowList.add(sublist);
        }

        inlineKeyboardMarkup.setKeyboard(rowList);

        SendMessage message = new SendMessage();
        message.setText(keyBoardName);
        message.setChatId(String.valueOf(chatId));
        message.setReplyMarkup(inlineKeyboardMarkup);
        return message;
    }

    public static SendDocument createDocument(long ChatId, String caption, InputFile sendFile) {
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(String.valueOf(ChatId));
        sendDocument.setCaption(caption);
        sendDocument.setDocument(sendFile);
        return sendDocument;
    }


    public static EditMessageReplyMarkup setButtons(Long chatId, int messageId, String[] buttons) {
        // Создаем клавиатуру
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(String.valueOf(chatId));
        editMessageReplyMarkup.setMessageId(messageId);

        KeyboardRow row = new KeyboardRow();
        List<String> callBacks = new ArrayList<>();
        for (String buttonText : buttons) {
            row.add(buttonText);
            callBacks.add("\\call_" + buttonText);
        }

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

        keyboardMarkup.setKeyboard(BotMessageService.createInlineKeyBoard(buttons, callBacks.toArray(new String[0])).getKeyboard());

        editMessageReplyMarkup.setReplyMarkup(keyboardMarkup);

        return editMessageReplyMarkup;

    }

    public static DeleteMessage deleteMessage(long ChatId, long MessageId) {
        return new DeleteMessage(
                String.valueOf(ChatId),
                (int) MessageId
        );
    }


}
