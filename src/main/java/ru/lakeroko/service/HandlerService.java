package ru.lakeroko.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.lakeroko.BotState;
import ru.lakeroko.dao.UserDaoImpl;
import ru.lakeroko.model.User;
import ru.lakeroko.utils.HandlerUtils;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

public class HandlerService {
    public final HandlerUtils handlerUtils;

    public HandlerService(TelegramClient telegramClient) {
        this.handlerUtils = new HandlerUtils(telegramClient);
    }

    public void handleStart(Update update) {
        Message message = update.getMessage();

        long chat_id = message.getChatId();
        BigInteger user_id = BigInteger.valueOf(message.getFrom().getId());

        User user = UserDaoImpl.findByUserId(user_id).orElseGet(() -> {
            User newUser = new User();
            newUser.setUserId(user_id);
            newUser.setUsername(message.getFrom().getUserName());

            return UserDaoImpl.save(newUser);
        });

        user.setState(BotState.AGREEMENT);
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
            handlerUtils.sendMessage(chat_id, "Шаг 4: Выберите ваш пол (Мужской/Женский):");
        } catch (DateTimeParseException e) {
            handlerUtils.deleteMessage(chat_id, message_id, 2);
            handlerUtils.sendMessage(chat_id, "Неверный формат даты. Введите дату в формате dd.MM.yyyy.");
        }
    }

    public void handleGender(CallbackQuery callbackQuery) {}

    public void handleCompeted(CallbackQuery callbackQuery) {}
    
    
}
