package ru.lakeroko;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import ru.lakeroko.dao.UserDaoImpl;
import ru.lakeroko.dto.CallbackQueryDto;
import ru.lakeroko.dto.MessageDto;
import ru.lakeroko.service.HandlerService;

import java.math.BigInteger;


public class TelegramFormBot implements LongPollingSingleThreadUpdateConsumer {
    private final HandlerService handlerService;
    private final UserDaoImpl userDao;

    public TelegramFormBot(String token) {
        this.userDao = new UserDaoImpl();
        this.handlerService = new HandlerService(new OkHttpTelegramClient(token), userDao);
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            MessageDto messageDto = new MessageDto(update.getMessage());

            if (messageDto.getText() != null){
                if (messageDto.getText().startsWith("/start")) {
                    String[] parts = update.getMessage().getText().split(" ");
                    String utm = null;

                    if (parts.length > 1)
                        utm = parts[1];

                    handlerService.handleStart(update, utm);

                    return;
                }
            }

            userDao.findByUserId(messageDto.getUser_id()).ifPresent(user -> {
                switch (user.getState()){
                    case FULL_NAME:
                        handlerService.handleFullName(messageDto);
                        break;
                    case BIRTH_DATE:
                        handlerService.handleBirthDate(messageDto);
                        break;
                    case PHOTO:
                        handlerService.handlePhoto(messageDto);
                        break;
                }
            });
        } else if (update.hasCallbackQuery()) {
            CallbackQueryDto callbackQueryDto = new CallbackQueryDto(update.getCallbackQuery());

            userDao.findByUserId(callbackQueryDto.getUser_id()).ifPresent(user -> {
                System.out.println(user.getUserId());
                switch (user.getState()){
                    case AGREEMENT:
                        handlerService.handleAgreement(callbackQueryDto);
                        break;
                    case GENDER:
                        handlerService.handleGender(callbackQueryDto);
                        break;
                }
            });
        }
    }
}
