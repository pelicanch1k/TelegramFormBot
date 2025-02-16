package ru.lakeroko.utils;

import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessages;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public void sendDocxFile(long chatId, byte[] docxBytes, String fileName) {
        InputFile docxInputFile = new InputFile(new ByteArrayInputStream(docxBytes), fileName);

        SendDocument sendDocument = SendDocument.builder()
                .chatId(chatId)
                .document(docxInputFile)
                .caption("Вот ваш .docx файл!")
                .build();

        try {
            telegramClient.execute(sendDocument); // Отправляем документ
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
        Dotenv dotenv = Dotenv.load();
        String botToken = dotenv.get("BOT_TOKEN");
        String fileUrl = "https://api.telegram.org/file/bot" + botToken + "/" + filePath;

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

    private void log(String username, String txt, String bot_answer) {
        System.out.println("\n ----------------------------");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        System.out.println(dateFormat.format(date));
        System.out.println("Message from " + username + "\t" + "Text - " + txt);
        System.out.println("Bot answer: \n Text - " + bot_answer);
    }

    public void execute(SendMessage new_message){
        try {
            telegramClient.execute(new_message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
