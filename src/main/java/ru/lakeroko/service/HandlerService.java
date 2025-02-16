package ru.lakeroko.service;

import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.lakeroko.BotState;
import ru.lakeroko.dao.UserDaoImpl;
import ru.lakeroko.dto.CallbackQueryDto;
import ru.lakeroko.dto.MessageDto;
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
    private final UserDaoImpl userDao;

    public HandlerService(TelegramClient telegramClient, UserDaoImpl userDao) {
        this.handlerUtils = new HandlerUtils(telegramClient);
        this.userDao = userDao;
    }

    public void handleStart(Update update, String utm) {
        Message message = update.getMessage();

        long chat_id = message.getChatId();
        BigInteger user_id = BigInteger.valueOf(message.getFrom().getId());

        User user = userDao.findByUserId(user_id).orElseGet(() -> {
            User newUser = new User();
            newUser.setUserId(user_id);
            newUser.setUsername(message.getFrom().getUserName());

            return userDao.create(newUser);
        });

        user.setState(BotState.AGREEMENT);

        user.setUtm(utm);

        userDao.update(user);

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

    public void handleAgreement(CallbackQueryDto callbackQueryDto) {
        userDao.findByUserId(callbackQueryDto.getUser_id()).ifPresent(user -> {
            user.setState(BotState.FULL_NAME);
            userDao.update(user);
        });

        String replyToUser = "Спасибо за согласие!\nШаг 2: Введите ваше ФИО (Отчество по желанию):";
        handlerUtils.sendMessage(callbackQueryDto.getChat_id(), replyToUser);
    }

    public void handleFullName(MessageDto messageDto) {
        long chat_id = messageDto.getChat_id();
        long message_id = messageDto.getMessage_id();
        String[] names = messageDto.getText().split(" ");

        if (names.length == 2 || names.length == 3) {
            userDao.findByUserId(messageDto.getUser_id()).ifPresent(user -> {
                user.setState(BotState.BIRTH_DATE);
                user.setFirstName(names[0]);
                user.setLastName(names[1]);

                if (names.length == 3) {
                    user.setMiddleName(names[2]);
                }

                userDao.update(user);
            });

            handlerUtils.deleteMessage(chat_id, message_id, 1);
            handlerUtils.sendMessage(chat_id, "Шаг 3: Введите вашу дату рождения в формате dd.MM.yyyy:");
        } else {
            handlerUtils.deleteMessage(chat_id, message_id, 2);
            handlerUtils.sendMessage(chat_id, "Пожалуйста введите ваше ФИО (Отчество по желанию):");
        }
    }

    public void handleBirthDate(MessageDto messageDto) {
        long chat_id = messageDto.getChat_id();
        long message_id = messageDto.getMessage_id();

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate birthDate = LocalDate.parse(messageDto.getText(), formatter);

            userDao.findByUserId(messageDto.getUser_id()).ifPresent(user -> {
                user.setState(BotState.GENDER);
                user.setBirthDate(birthDate);

                userDao.update(user);
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

    public void handleGender(CallbackQueryDto callbackQueryDto) {
        userDao.findByUserId(callbackQueryDto.getUser_id()).ifPresent(user -> {
            user.setState(BotState.PHOTO);
            user.setGender(callbackQueryDto.getCallback_data());

            userDao.update(user);
        });

        handlerUtils.sendMessage(callbackQueryDto.getChat_id(), "Шаг 5: Теперь отправьте вашу фотографию");
    }

    public void handlePhoto(MessageDto messageDto) {
        String fileId = messageDto.getPhotos().stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .map(PhotoSize::getFileId)
                .orElse("");

        if (!fileId.isEmpty()) {
            try {
                String filePath = handlerUtils.getFilePath(new GetFile(fileId));

                byte[] fileBytes = handlerUtils.downloadFileAsBytes(filePath);

                userDao.findByUserId(messageDto.getUser_id()).ifPresent(user -> {
                    user.setState(BotState.COMPLETED);
                    user.setPhoto(fileBytes);

                    userDao.update(user);
                });

                handlerUtils.deleteMessage(messageDto.getChat_id(), messageDto.getMessage_id(), 8);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
