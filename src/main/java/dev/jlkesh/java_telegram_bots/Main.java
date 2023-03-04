package dev.jlkesh.java_telegram_bots;

import com.pengrad.telegrambot.UpdatesListener;
import dev.jlkesh.java_telegram_bots.config.TelegramBotConfiguration;
import dev.jlkesh.java_telegram_bots.handlers.UpdateHandler;
import org.mindrot.jbcrypt.BCrypt;

public class Main {
    public static void main(String[] args) {
        UpdateHandler updateHandler = new UpdateHandler();
        TelegramBotConfiguration.get().setUpdatesListener((updates) -> {
            updateHandler.handle(updates);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}