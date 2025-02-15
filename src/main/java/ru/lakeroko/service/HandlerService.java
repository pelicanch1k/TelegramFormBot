package ru.lakeroko.service;

import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.lakeroko.BotState;
import ru.lakeroko.dao.UserDaoImpl;
import ru.lakeroko.model.User;
import ru.lakeroko.utils.HandlerUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class HandlerService {
    public final HandlerUtils handlerUtils;

    public HandlerService(TelegramClient telegramClient) {
        this.handlerUtils = new HandlerUtils(telegramClient);
    }

    public void handleStart(Update update, String utm) {
        Message message = update.getMessage();

        long chat_id = message.getChatId();
        BigInteger user_id = BigInteger.valueOf(message.getFrom().getId());

        User user = UserDaoImpl.findByUserId(user_id).orElseGet(() -> {
            User newUser = new User();
            newUser.setUserId(user_id);
            newUser.setUsername(message.getFrom().getUserName());

            return UserDaoImpl.create(newUser);
        });

        user.setState(BotState.AGREEMENT);

        user.setUtm(utm);

        UserDaoImpl.update(user);

        SendMessage new_message = SendMessage.builder()
                .chatId(chat_id)
                .text("Шаг 1: Согласие на обработку персональных данных.\nНажмите кнопку 'Согласен', чтобы продолжить.")
                .replyMarkup(
                        InlineKeyboardMarkup.builder()
                                .keyboard(Arrays.asList(
                                        handlerUtils.getInlineKeyboardRow("Согласен", "agreement"),
                                        new InlineKeyboardRow(InlineKeyboardButton
                                                .builder()
                                                .text("Посмотреть соглашение")
                                                .url("https://www.google.com")  // Указываем URL для перехода
                                                .build())
                                )).build())
                .build();

        handlerUtils.execute(new_message);
    }

    public void handleAgreement(CallbackQuery callbackQuery) {
        if (callbackQuery.getData().equals("agreement")) {
            long chat_id = callbackQuery.getMessage().getChatId();
            long message_id = callbackQuery.getMessage().getMessageId();

            BigInteger user_id = BigInteger.valueOf(callbackQuery.getMessage().getChat().getId());

            UserDaoImpl.findByUserId(user_id).ifPresent(user -> {
                user.setState(BotState.FULL_NAME);
                UserDaoImpl.update(user);
            });

            String replyToUser = "Спасибо за согласие!\nШаг 2: Введите ваше ФИО (Отчество по желанию):";
            handlerUtils.sendMessage(chat_id, replyToUser);
        }
    }

    public void handleFullName(Message message) {
        long chat_id = message.getChatId();
        long message_id = message.getMessageId();
        String[] names = message.getText().split(" ");

        if (names.length == 2 || names.length == 3) {
            BigInteger user_id = BigInteger.valueOf(message.getFrom().getId());

            UserDaoImpl.findByUserId(user_id).ifPresent(user -> {
                user.setState(BotState.BIRTH_DATE);
                user.setFirstName(names[0]);
                user.setLastName(names[1]);

                if (names.length == 3) {
                    user.setMiddleName(names[2]);
                }

                UserDaoImpl.update(user);
            });

            handlerUtils.deleteMessage(chat_id, message_id, 1);
            handlerUtils.sendMessage(chat_id, "Шаг 3: Введите вашу дату рождения в формате dd.MM.yyyy:");
        } else {
            handlerUtils.deleteMessage(chat_id, message_id, 2);
            handlerUtils.sendMessage(chat_id, "Пожалуйста введите ваше ФИО (Отчество по желанию):");
        }
    }

    public void handleBirthDate(Message message) {
        long chat_id = message.getChatId();
        long message_id = message.getMessageId();
        String text = message.getText();

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate birthDate = LocalDate.parse(text, formatter);

            BigInteger user_id = BigInteger.valueOf(message.getFrom().getId());

            UserDaoImpl.findByUserId(user_id).ifPresent(user -> {
                user.setState(BotState.GENDER);
                user.setBirthDate(birthDate);

                UserDaoImpl.update(user);
            });

            handlerUtils.deleteMessage(chat_id, message_id, 1);
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(chat_id)
                    .text("Шаг 4: Выберите ваш пол (Мужской/Женский):")
                    .replyMarkup(
                            InlineKeyboardMarkup.builder()
                                    .keyboard(Arrays.asList(
                                            handlerUtils.getInlineKeyboardRow("Мужской", "male"),
                                            handlerUtils.getInlineKeyboardRow("Женский", "female")
                                    )).build())
                    .build();

            handlerUtils.execute(sendMessage);
        } catch (DateTimeParseException e) {
            handlerUtils.deleteMessage(chat_id, message_id, 5);
            handlerUtils.sendMessage(chat_id, "Неверный формат даты. Введите дату в формате dd.MM.yyyy.");
        }
    }

    public void handleGender(CallbackQuery callbackQuery) {
        long chat_id = callbackQuery.getMessage().getChatId();
        long message_id = callbackQuery.getMessage().getMessageId();

        BigInteger user_id = BigInteger.valueOf(callbackQuery.getMessage().getChat().getId());
        String gender = callbackQuery.getData();

        UserDaoImpl.findByUserId(user_id).ifPresent(user -> {
            user.setState(BotState.PHOTO);
            user.setGender(gender);

            UserDaoImpl.update(user);
        });

        handlerUtils.sendMessage(chat_id, "Шаг 5: Теперь отправьте вашу фотографию");
    }

    public void handleCompeted(Message message) {
        long chat_id = message.getChatId();
        long message_id = message.getMessageId();
        BigInteger user_id = BigInteger.valueOf(message.getFrom().getId());

        // Получаем список фотографий
        List<PhotoSize> photos = message.getPhoto();

        // Находим фото с наибольшим размером (самое качественное)
        String fileId = photos.stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .map(PhotoSize::getFileId)
                .orElse("");

        if (!fileId.isEmpty()) {
            try {
                String filePath = handlerUtils.getFilePath(new GetFile(fileId));

                byte[] fileBytes = downloadFileAsBytes(filePath);

                UserDaoImpl.findByUserId(user_id).ifPresent(user -> {
                    user.setState(BotState.COMPLETED);
                    user.setPhoto(fileBytes);
                    user.setState(BotState.COMPLETED);

                    UserDaoImpl.update(user);
                });

                User user = UserDaoImpl.findByUserId(user_id).get();

                StringBuilder caption = new StringBuilder("Ваши данные:\n" +
                        "Имя: " + user.getFirstName() + "\n"+
                        "Фамилия: " + user.getLastName() + "\n");

                if (user.getMiddleName() != null) {
                    caption.append("Отчество: ").append(user.getMiddleName()).append("\n");
                }
                caption.append("Пол: ").append(user.getGender()).append("\n");

                caption.append("Дата рождения: ");
                caption.append(user.getBirthDate());

                handlerUtils.deleteMessage(chat_id, message_id, 8);
                handlerUtils.sendPhotoFromBytes(chat_id, user.getPhoto(), caption.toString());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Метод для скачивания файла и преобразования в byte[]
    private byte[] downloadFileAsBytes(String filePath) throws IOException {
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
    
}
