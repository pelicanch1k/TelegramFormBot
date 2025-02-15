package ru.lakeroko.utils;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessages;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

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

    public void sendPhoto(Long chat_id, String f_id) {
        execute(SendPhoto
                .builder()
                .chatId(chat_id)
                .photo(new InputFile(f_id))
                .build());
    }

    // Метод для отправки фото из byte[]
    public void sendPhotoFromBytes(long chatId, byte[] photoBytes, String caption) {
        // Создаем InputFile из byte[]
        InputFile photoInputFile = new InputFile(new ByteArrayInputStream(photoBytes), "photo.jpg");

        // Создаем объект SendPhoto
        SendPhoto sendPhoto = new SendPhoto(String.valueOf(chatId), photoInputFile);
        sendPhoto.setCaption(caption); // Опционально: добавляем подпись

        execute(sendPhoto);
    }

    public void deleteMessage(Long chatId, Long messageId, int count) {
        List<Integer> list = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            list.add(Integer.valueOf(String.valueOf(messageId-i)));
        }

        execute(DeleteMessages.builder().chatId(chatId).messageIds(list).build());
    }

    public String getFilePath(GetFile getFile) {
        try {
            return telegramClient.execute(getFile).getFilePath(); // Sending our message object to user
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void execute(EditMessageText new_message){
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

    private void execute(DeleteMessages deleteMessage){
        try {
            telegramClient.execute(deleteMessage); // Sending our message object to user
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void execute(SendPhoto new_message){
        try {
            telegramClient.execute(new_message); // Sending our message object to user
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
