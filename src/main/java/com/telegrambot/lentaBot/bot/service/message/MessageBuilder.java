package com.telegrambot.lentaBot.bot.service.message;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class MessageBuilder {

    public MessageBuilder() {
    }

    public SendMediaBotMethod createMessage(Message message)
    {
        if(message.getMediaGroupId() == null)
        {


            if(message.hasPhoto())
            {
                SendPhoto photo = new SendPhoto();
                photo.setCaption(message.getCaption());
                photo.setPhoto(new InputFile(message.getPhoto().get(0).getFileId()));
                return photo;
            }
            if(message.hasVideo())
            {
                SendVideo video = new SendVideo();
                video.setCaption(message.getCaption());
                video.setVideo(new InputFile(message.getVideo().getFileId()));
                return video;
            }
            if(message.hasAudio())
            {
                SendAudio audio = new SendAudio();
                audio.setCaption(message.getCaption());
                audio.setAudio(new InputFile(message.getAudio().getFileId()));
                return audio;
            }

            if(message.hasAnimation())
            {
                SendAnimation animation = new SendAnimation();
                animation.setCaption(message.getCaption());
                animation.setAnimation(new InputFile(message.getAnimation().getFileId()));
                return animation;
            }
            if(message.hasDocument())
            {
                SendDocument document = new SendDocument();
                document.setCaption(message.getCaption());
                document.setDocument(new InputFile(message.getDocument().getFileId()));

                return document;
            }
            if(message.hasSticker())
            {
                SendSticker sticker = new SendSticker();
                sticker.setSticker(new InputFile(message.getSticker().getFileId()));
                return sticker;
            }
        }
        else
        {
            throw new IllegalArgumentException("Message Is MediaGroup! " + message.getMessageId());
        }
        return null;
    }

    public BotApiMethodMessage createApiMessage(Message message)
    {
        if(message.getMediaGroupId() == null)
        {
            if(message.hasVideoNote() || message.hasVoice() || message.hasPoll() || message.hasLocation())
            {
                ForwardMessage forwardMessage = new ForwardMessage();
                forwardMessage.setFromChatId(message.getChatId());
                forwardMessage.setMessageId(message.getMessageId());
                forwardMessage.setMessageThreadId(message.getMessageThreadId());
                return forwardMessage;
            }
            if(message.hasText())
            {
                SendMessage text = new SendMessage();
                text.setText(message.getText());
                text.disableWebPagePreview();
                return text;
            }
        }
        else
        {
            throw new IllegalArgumentException("Message Is MediaGroup! " + message.getMessageId());
        }
        return null;
    }
}
