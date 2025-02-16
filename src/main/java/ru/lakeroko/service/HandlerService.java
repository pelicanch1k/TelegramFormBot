package ru.lakeroko.service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
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

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Comparator;

public class HandlerService {
    private static final Logger logger = LoggerFactory.getLogger(HandlerService.class);
    public final HandlerUtils handlerUtils;
    private final UserDaoImpl userDao;

    public HandlerService(TelegramClient telegramClient, UserDaoImpl userDao) {
        this.handlerUtils = new HandlerUtils(telegramClient);
        this.userDao = userDao;
    }

    public void handleStart(MessageDto messageDto, String utm) {
        long chat_id = messageDto.getChat_id();
        BigInteger user_id = messageDto.getUser_id();

        logger.info("Handling start for user_id: {}", user_id);

        User user = userDao.findByUserId(user_id).orElseGet(() -> {
            User newUser = new User();
            newUser.setUserId(user_id);
            newUser.setUsername(messageDto.getUsername());
            newUser.setUtm(utm);

            return userDao.create(newUser);
        });

        if (utm != null && !utm.equals(user.getUtm())) {
            handlerUtils.sendMessage(chat_id, "У вас уже есть utm метка");
        }

        user.setState(BotState.AGREEMENT);

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
        final String LETTERS_ONLY_REGEX = "^[a-zA-Z]+$";

        long chat_id = messageDto.getChat_id();
        long message_id = messageDto.getMessage_id();
        String[] names = messageDto.getText().split(" ");

        logger.info("Handling full name for chat_id: {}, message_id: {}", chat_id, message_id);

        if (names.length == 2 || names.length == 3) {
            userDao.findByUserId(messageDto.getUser_id()).ifPresent(user -> {
                if (names[0].matches(LETTERS_ONLY_REGEX) && names[1].matches(LETTERS_ONLY_REGEX)) {
                    if (names.length == 3) {
                        if (names[2].matches(LETTERS_ONLY_REGEX))
                            user.setMiddleName(names[2]);
                        else {
                            handlerUtils.deleteMessage(chat_id, message_id, 2);
                            handlerUtils.sendMessage(chat_id, "Шаг 2: Пожалуйста введите только буквы");
                            return;
                        }
                    }

                    user.setFirstName(names[0]);
                    user.setLastName(names[1]);
                    user.setState(BotState.BIRTH_DATE);

                    userDao.update(user);

                    handlerUtils.deleteMessage(chat_id, message_id, 1);
                    handlerUtils.sendMessage(chat_id, "Шаг 3: Введите вашу дату рождения в формате dd.MM.yyyy:");
                } else {
                    handlerUtils.deleteMessage(chat_id, message_id, 2);
                    handlerUtils.sendMessage(chat_id, "Шаг 2: Пожалуйста введите только буквы");
                }
            });
        } else {
            handlerUtils.deleteMessage(chat_id, message_id, 2);
            handlerUtils.sendMessage(chat_id, "Шаг 2: Пожалуйста введите ваше ФИО (Отчество по желанию):");
        }
    }

    public void handleBirthDate(MessageDto messageDto) {
        long chat_id = messageDto.getChat_id();
        long message_id = messageDto.getMessage_id();

        logger.info("Handling birth date for chat_id: {}, message_id: {}", chat_id, message_id);

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
            logger.error("Invalid birth date format", e);
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

                WordDocumentService wordDocumentService = new WordDocumentService(userDao.findByUserId(messageDto.getUser_id()).get());
                byte[] docx = wordDocumentService.createDocx();

                handlerUtils.sendDocxFile(messageDto.getChat_id(), docx, messageDto.getUsername()+".docx");

                wordDocumentService.deleteDocxFile();

            } catch (IOException e) {
                logger.error("Error handling photo", e);
            } catch (InvalidFormatException e) {
                logger.error("Invalid format exception", e);
            }
        }
    }
}