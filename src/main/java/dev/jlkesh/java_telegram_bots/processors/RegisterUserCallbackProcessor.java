package dev.jlkesh.java_telegram_bots.processors;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import dev.jlkesh.java_telegram_bots.config.TelegramBotConfiguration;
import dev.jlkesh.java_telegram_bots.daos.UserDao;
import dev.jlkesh.java_telegram_bots.domains.UserDomain;
import dev.jlkesh.java_telegram_bots.steps.DefaultState;
import dev.jlkesh.java_telegram_bots.steps.RegistrationState;
import dev.jlkesh.java_telegram_bots.utils.AnswerCallbackQueryFactory;
import dev.jlkesh.java_telegram_bots.utils.SendMessageFactory;

import java.util.HashMap;
import java.util.Map;

import static dev.jlkesh.java_telegram_bots.config.ThreadSafeBeansContainer.*;
import static dev.jlkesh.java_telegram_bots.utils.MessageSourceUtils.*;

public class RegisterUserCallbackProcessor implements Processor<RegistrationState> {
    private final TelegramBot bot = TelegramBotConfiguration.get();

    public static final Map<Long, StringBuilder> userRegisterPassword = new HashMap<>();

    @Override
    public void process(Update update, RegistrationState state) {
        CallbackQuery callbackQuery = update.callbackQuery();
        Message message = callbackQuery.message();
        int messageId = message.messageId();

        Long chatID = callbackQuery.message().chat().id();
        UserDomain user=new UserDao().getUser(String.valueOf(chatID));
        String data = callbackQuery.data();
        String language=user.getLanguage();

        if (!userService.get().isRegistered(String.valueOf(chatID))) {
            if (RegistrationState.PASSWORD.equals(state)) {
                if (data.equals("done")) {
                    StringBuilder password = userRegisterPassword.get(chatID);
                    if (password.length() == 4) {

                        UserDomain domain = UserDomain.builder()
                                .chatID(chatID.toString())
                                .username(usernames.get(String.valueOf(chatID)))
                                .password(userRegisterPassword.get(chatID).toString())
                                .firstName(update.callbackQuery().from().firstName())
                                .build();
                        userService.get().create(domain);
                        bot.execute(new SendMessage(chatID,  getLocalizedMessage("register.success", "en")+
                                getLocalizedMessage("data.generate.infomessage", "en")));
                        bot.execute(new DeleteMessage(chatID, messageId));
                        userState.put(chatID, DefaultState.NOTHING);
                    } else
                        bot.execute(AnswerCallbackQueryFactory.answerCallbackQuery(callbackQuery.id(), getLocalizedMessage("password.must.contain", "en")));
                } else if ("d".equals(data)) {

                    StringBuilder password = userRegisterPassword.get(chatID);
                    int length = password.length();
                    if (length == 0)
                        bot.execute(AnswerCallbackQueryFactory.answerCallbackQuery(callbackQuery.id(), getLocalizedMessage("password.empty", language)));
                    else {
                        length = length - 1;
                        String messageText = getLocalizedMessage("enter.password", "en") + "*️⃣".repeat(length) + " _ ".repeat(4 - length);
                        bot.execute(SendMessageFactory.getEditMessageTextForPassword(chatID, messageId, messageText));
                        state.setPassword(password.deleteCharAt(length));
                    }
                } else if ("hide".equals(data)) {

                    StringBuilder password = userRegisterPassword.get(chatID);
                    int length = password.length();
                    if (length == 0)
                        bot.execute(AnswerCallbackQueryFactory.answerCallbackQuery(callbackQuery.id(), "Password field is empty"));
                    else {
//                        length = length - 1;
                        String messageText = getLocalizedMessage("enter.password", "en") + "*️⃣".repeat(length) + " _ ".repeat(4 - length);
                        bot.execute(SendMessageFactory.getEditMessageTextForPassword(chatID, messageId, messageText));
                        state.setPassword(password.deleteCharAt(length));
                    }
                } else if ("show".equals(data)) {

                    String messageText = getLocalizedMessage("enter.password", "en") + userRegisterPassword.get(chatID);
                    bot.execute(SendMessageFactory.getEditMessageTextForPassword(chatID, messageId, messageText));
                }  else {

                    StringBuilder password = userRegisterPassword.get(chatID);
                    int length = password.length();
                    if (length <= 3) {
                        String messageText = getLocalizedMessage("enter.password", "en") + "*️⃣".repeat(length + 1) + " _ ".repeat(3 - length);
                        bot.execute(SendMessageFactory.getEditMessageTextForPassword(chatID, messageId, messageText));
                        password.append(data);
                    } else
                        bot.execute(AnswerCallbackQueryFactory.answerCallbackQuery(callbackQuery.id(), getLocalizedMessage("password.full", "en")));
                }
            }
        }
    }

}
