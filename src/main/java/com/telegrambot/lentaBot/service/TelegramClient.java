package com.telegrambot.lentaBot.service;

import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.Client;
import org.springframework.stereotype.Service;

@Service
public class TelegramClient {
    private final Client client;

    // Обработчик обновлений от клиента
    public TelegramClient() {
        // Инициализация клиента
        client = Client.create(new UpdateHandler(), null, null);
    }

    public class UpdateHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            if (object instanceof TdApi.UpdateAuthorizationState) {
                handleAuthorizationState((TdApi.UpdateAuthorizationState) object);
            } else if (object instanceof TdApi.UpdateUser) {
                handleUserUpdate((TdApi.UpdateUser) object);
            }
            // Обработка других обновлений по мере необходимости
        }

    }

    // Обработка состояния авторизации
    private void handleAuthorizationState(TdApi.UpdateAuthorizationState update) {
        switch (update.authorizationState.getConstructor()) {
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR:
                System.out.println("Введите номер телефона:");
                break;
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR:
                System.out.println("Введите код из SMS:");
                break;
            case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                System.out.println("Успешно авторизован!");
                client.send(new TdApi.GetMe(), this::handleGetMe);
                break;
            default:
                System.out.println("Неизвестное состояние авторизации");
        }
    }

    // Обработка обновлений пользователя
    private void handleUserUpdate(TdApi.UpdateUser update) {
        System.out.println("Обновлен пользователь: " + update.user.firstName + " " + update.user.lastName);
    }

    // Обработка запроса на получение информации о пользователе
    private void handleGetMe(Object result) {
        if (result instanceof TdApi.User) {
            TdApi.User user = (TdApi.User) result;
            System.out.println("Имя пользователя: " + user.firstName + " " + user.lastName);
        }
    }

    // Метод для начала процесса авторизации
    public void start(String phoneNumber) {
        client.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null), null);
    }

}
