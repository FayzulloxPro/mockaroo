package dev.jlkesh.java_telegram_bots.processors;

import com.pengrad.telegrambot.model.Update;
import dev.jlkesh.java_telegram_bots.steps.State;

public interface Processor<S> {
    void process(Update update, S state);
}
