package dev.jlkesh.java_telegram_bots.utils;

import lombok.NonNull;

import java.util.Locale;
import java.util.ResourceBundle;

public class ButtonMessageSourceUtils {
    private static final ThreadLocal<ResourceBundle> en = ThreadLocal.withInitial(() -> ResourceBundle.getBundle("buttonMessages"));
    private static final ThreadLocal<ResourceBundle> uz = ThreadLocal.withInitial(() -> ResourceBundle.getBundle("buttonMessages", Locale.forLanguageTag("uz")));
    private static final ThreadLocal<ResourceBundle> ru = ThreadLocal.withInitial(() -> ResourceBundle.getBundle("buttonMessages", Locale.forLanguageTag("ru")));

    public static String getLocalizedMessage(@NonNull String key, @NonNull String language) {
        return switch (language){
            case "uz" -> uz.get().getString(key);
            case "ru" -> ru.get().getString(key);
            default -> en.get().getString(key);
        };

    }
}