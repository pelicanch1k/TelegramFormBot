package ru.lakeroko.utils;

import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessages;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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

    public void sendMessage(Long chat_id, String text) {
        execute(SendMessage.builder()
                .chatId(chat_id)
                .text(text)
                .build());
    }

    // Метод для отправки фото из byte[]
    public void sendPhoto(long chatId, byte[] photoBytes, String caption) {
        // Создаем InputFile из byte[]
        InputFile photoInputFile = new InputFile(new ByteArrayInputStream(photoBytes), "photo.jpg");

        // Создаем объект SendPhoto
        SendPhoto sendPhoto = new SendPhoto(String.valueOf(chatId), photoInputFile);
        sendPhoto.setCaption(caption); // Опционально: добавляем подпись

        try {
            telegramClient.execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void deleteMessage(Long chatId, Long messageId, int count) {
        List<Integer> list = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            list.add(Integer.valueOf(String.valueOf(messageId-i)));
        }

        try {
            telegramClient.execute(DeleteMessages.builder().chatId(chatId).messageIds(list).build());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getFilePath(GetFile getFile) {
        try {
            return telegramClient.execute(getFile).getFilePath();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return null;
    }

    public byte[] downloadFileAsBytes(String filePath) throws IOException {
        String fileUrl = "https://api.telegram.org/file/bot" + "1952018700:AAEZL8jFwg018V9li_tbrVZJfkq0dcvVR7s" + "/" + filePath;

        try (InputStream in = new URL(fileUrl).openStream();
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            return buffer.toByteArray();
        }
    }

    public void execute(SendMessage new_message){
        try {
            telegramClient.execute(new_message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
