package ru.lakeroko.dto;

import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.math.BigInteger;
import java.util.List;

@Data
public class MessageDto {
    private String text;
    private BigInteger user_id;
    private long message_id;
    private long chat_id;
    private String username;
    private List<PhotoSize> photos;

    public MessageDto(Message message) {
        text = message.getText();
        chat_id = message.getChat().getId();
        message_id = message.getMessageId();
        user_id = BigInteger.valueOf(message.getFrom().getId());
        username = message.getFrom().getUserName();

        if (message.hasPhoto()){
            photos = message.getPhoto();
        } else {
            photos = null;
        }
    }
}
