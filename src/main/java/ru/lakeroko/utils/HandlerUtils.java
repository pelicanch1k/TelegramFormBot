package ru.lakeroko.utils;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static java.lang.Math.toIntExact;

public class HandlerUtils {
    private final TelegramClient telegramClient;

    public HandlerUtils(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    public InlineKeyboardRow getInlineKeyboardRow(String text, String callbackData) {
        return new InlineKeyboardRow(InlineKeyboardButton
                .builder()
                .text(text)
                .callbackData(callbackData)
                .build()
        );
    }

    public void sendEditMessage(Long chat_id, Long message_id, String text) {
        execute(EditMessageText.builder()
                .chatId(chat_id)
                .messageId(toIntExact(message_id))
                .text(text)
                .build());
    }

    public void sendMessage(Long chat_id, String text) {
        execute(SendMessage.builder()
                .chatId(chat_id)
                .text(text)
                .build());
    }

    public void deleteMessage(Long chatId, Long messageId, int count) {
        --count;
        for (int i = 0; i <= count; i++) {
            execute(DeleteMessage.builder()
                    .messageId(toIntExact(messageId - i))
                    .chatId(chatId)
                    .build());
        }
    }

    public void execute(EditMessageText new_message){
        try {
            telegramClient.execute(new_message); // Sending our message object to user
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void execute(SendMessage new_message){
        try {
            telegramClient.execute(new_message); // Sending our message object to user
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void execute(DeleteMessage deleteMessage){
        try {
            telegramClient.execute(deleteMessage); // Sending our message object to user
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
