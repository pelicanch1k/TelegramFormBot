package ru.lakeroko;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import ru.lakeroko.dao.UserDaoImpl;
import ru.lakeroko.service.HandlerService;

import java.math.BigInteger;


public class TelegramFormBot implements LongPollingSingleThreadUpdateConsumer {
    private final HandlerService handlerService;

    public TelegramFormBot(String token) {
        this.handlerService = new HandlerService(new OkHttpTelegramClient(token));
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            String messageText = update.getMessage().getText();

            if (messageText != null){
                if (messageText.startsWith("/start")) {
                    String[] parts = update.getMessage().getText().split(" ");
                    String utm = null;

                    if (parts.length > 1)
                        utm = parts[1];

                    handlerService.handleStart(update, utm);

                    return;
                }
            }

            System.out.println(message.getMessageId());
            BigInteger user_id = BigInteger.valueOf(message.getFrom().getId());

            UserDaoImpl.findByUserId(user_id).ifPresent(user -> {
                switch (user.getState()){
                    case FULL_NAME:
                        handlerService.handleFullName(message);
                        break;
                    case BIRTH_DATE:
                        handlerService.handleBirthDate(message);
                        break;
                    case PHOTO:
                        handlerService.handleCompeted(message);
                        break;
                }
            });
        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            BigInteger user_id = BigInteger.valueOf(callbackQuery.getFrom().getId());

            UserDaoImpl.findByUserId(user_id).ifPresent(user -> {
                System.out.println(user.getUserId());
                switch (user.getState()){
                    case AGREEMENT:
                        handlerService.handleAgreement(callbackQuery);
                        break;
                    case GENDER:
                        handlerService.handleGender(callbackQuery);
                        break;
                }
            });
        }
    }
}
