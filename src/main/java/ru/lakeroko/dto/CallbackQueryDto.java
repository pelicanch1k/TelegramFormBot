package ru.lakeroko.dto;

import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.math.BigInteger;

@Data
public class CallbackQueryDto {
    private long chat_id;
    private long message_id;
    private BigInteger user_id;
    private String callback_data;

    public CallbackQueryDto(CallbackQuery callbackQuery) {
        this.chat_id = callbackQuery.getMessage().getChatId();
        this.message_id = callbackQuery.getMessage().getMessageId();
        this.user_id = BigInteger.valueOf(callbackQuery.getMessage().getChat().getId());
        this.callback_data = callbackQuery.getData();
    }
}
