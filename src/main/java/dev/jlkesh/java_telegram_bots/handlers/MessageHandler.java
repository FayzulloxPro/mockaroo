package dev.jlkesh.java_telegram_bots.handlers;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ForceReply;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import dev.jlkesh.java_telegram_bots.config.TelegramBotConfiguration;
import dev.jlkesh.java_telegram_bots.daos.UserDao;
import dev.jlkesh.java_telegram_bots.dataserver.FakerApplicationGenerateRequest;
import dev.jlkesh.java_telegram_bots.domains.UserDomain;
import dev.jlkesh.java_telegram_bots.dtos.FieldDTO;
import dev.jlkesh.java_telegram_bots.processors.RegisterUserCallbackProcessor;
import dev.jlkesh.java_telegram_bots.steps.DefaultState;
import dev.jlkesh.java_telegram_bots.steps.GenerateDataState;
import dev.jlkesh.java_telegram_bots.steps.RegistrationState;
import dev.jlkesh.java_telegram_bots.steps.State;
import dev.jlkesh.java_telegram_bots.utils.InlineKeyboardMarkupFactory;

import java.util.Objects;

import static dev.jlkesh.java_telegram_bots.config.ThreadSafeBeansContainer.*;
import static dev.jlkesh.java_telegram_bots.utils.MessageSourceUtils.getLocalizedMessage;


/*@Slf4j*/
public class MessageHandler implements Handler {
    private final TelegramBot bot = TelegramBotConfiguration.get();

    @Override
    public void handle(Update update) {
        Message message = update.message();
        Chat chat = message.chat();
        Long chatID = chat.id();
        String text = message.text();
        State step = userState.get(chatID);

        UserDao dao = new UserDao();
        UserDomain user = dao.getUser(String.valueOf(chatID));
        startGenerateMockData(chatID, user);
        /*if ("/history".equals(text)) {

            FileDAO fileDAO = FileDAO.getInstance();
            List<FIleDomain> files = fileDAO.getFiles(1, 10, String.valueOf(chatID));

            StringBuilder st = new StringBuilder("Here is your files: ");
            for (int i = 0; i < files.size(); i++) {
                FIleDomain file = files.get(i);
                st.append(i + 1).append(") ")
                        .append("File name: ").append(file.getFileName()).append(", rows: ").append(file.getRowsCount()).append("\n");
            }

            SendMessage sendMessage = new SendMessage(chatID, st.toString());

            sendMessage.replyMarkup(InlineKeyboardMarkupFactory.getHistory(1, String.valueOf(chatID), files));
            bot.execute(sendMessage);

            bot.execute(new SendMessage(chatID, "test").replyMarkup(InlineKeyboardMarkupFactory.enterPasswordKeyboard()));
        } else if (text.equals("/language") && userService.get().isRegistered(String.valueOf(chatID))) {

            bot.execute(new SendMessage(chatID, getLocalizedMessage("l.choose.lang", user.getLanguage())).replyMarkup(
                    InlineKeyboardMarkupFactory.languages(user.getLanguage())
            ));
            userState.put(chatID, GenerateDataState.MENU);
            // TODO: 2/13/2023 String tipida language berish kerak languages keyboardi uchun

        } else if ((step == null || text.equals("/start"))) {
            if (!userService.get().isRegistered(String.valueOf(chatID))) {
                startRegister(chatID, user);
            } else {
                userState.put(chatID, NOTHING);
                bot.execute(new SendMessage(chatID, getLocalizedMessage("data.generate.infomessage", user.getLanguage())));
            }
        } else if (step.equals(NOTHING) && "/generate".equals(text)) {
            startGenerateMockData(chatID, user);


        } else {

            if (step instanceof RegistrationState regState) {
                if (regState.equals(RegistrationState.USERNAME)) {
                    usernames.put(String.valueOf(chatID), text);
                } else if (regState.equals(RegistrationState.PASSWORD)) {
                    bot.execute(new SendMessage(chatID, "Please set password"));
                    deleteMessage(message, chatID);
                }
                registration(message, chatID, regState, user);

            } else if (step instanceof DefaultState defaultState) {
//                if (defaultState.equals(DefaultState.DELETE)) {
//                    deleteMessage(message, chatID);
//                }
            } else if (step instanceof GenerateDataState genState) {
                if (genState.equals(GenerateDataState.GET_FILENAME)) {

                    getFileName(chatID, text, user);

                } else if (genState.equals(GenerateDataState.COUNT)) {

                    getCountOfRows(chatID, text, user);

                } else if (genState.equals(GenerateDataState.FIELD_NAME)) {

                    getFieldName(chatID, text, user);
                }
            }
        }*/
    }


    private void getFieldName(Long chatID, String text, UserDomain userDomain) {
        String fieldName = getValidatedFieldName(text);
        if (Objects.isNull(fieldName) || text.contains(" ")) {
            bot.execute(new SendMessage(chatID, getLocalizedMessage("data.generate.invalid.field.name", userDomain.getLanguage())));
        } else {
            FieldDTO fieldDTO = userDataFields.get(String.valueOf(chatID));
            fieldDTO.setFieldName(text);
            userState.put(chatID, GenerateDataState.FIELD_TYPE);
            SendMessage sendMessage = new SendMessage(chatID, getLocalizedMessage("choose.field", userDomain.getLanguage()));
            sendMessage.replyMarkup(InlineKeyboardMarkupFactory.getFieldTypes(userDomain.getLanguage()));
            bot.execute(sendMessage);
        }
    }


    private String getValidatedFieldName(String fieldName) {
        if (!fieldName.contains(" ")) {
            return fieldName;
        }
        return null;
    }

    private void getCountOfRows(Long chatID, String text, UserDomain userDomain) {
        try {
            int count = Integer.parseInt(text);

            if (count < 0 || count > 20000) {
                bot.execute(new SendMessage(chatID, getLocalizedMessage("invalid.rows", userDomain.getLanguage())));
                return;
            }
            FakerApplicationGenerateRequest request = userDataRequests.get(chatID);
            if (Objects.isNull(request)) {
                request = new FakerApplicationGenerateRequest();
            }
            request.setCount(count);

            userState.put(chatID, GenerateDataState.MENU);
            bot.execute(new SendMessage(chatID, getLocalizedMessage("number.of.rows.accepted", userDomain.getLanguage())));
            SendMessage sendMessage = new SendMessage(chatID, getLocalizedMessage("complete.form", userDomain.getLanguage()));
            sendMessage.replyMarkup(InlineKeyboardMarkupFactory.mainMenu(request, userDomain.getLanguage()));
            bot.execute(sendMessage);
        } catch (Exception e) {
            bot.execute(new SendMessage(chatID, getLocalizedMessage("invalid.rows.space", userDomain.getLanguage())));
        }
    }

    private void getFileName(Long chatID, String text, UserDomain userDomain) {
        String fileName = getValidatedFileName(text);
        if (Objects.isNull(fileName)) {
            bot.execute(new SendMessage(chatID, getLocalizedMessage("invalid.file.name", userDomain.getLanguage())));
        } else {
            // TODO: 2/5/2023 filename


            FakerApplicationGenerateRequest request = userDataRequests.get(chatID);

            if (Objects.isNull(request)) {
                request = new FakerApplicationGenerateRequest();
            }

            request.setFileName(fileName);
            userDataFields.put(String.valueOf(chatID), new FieldDTO());

            bot.execute(new SendMessage(chatID, getLocalizedMessage("valid.file.name", userDomain.getLanguage())));
            userState.put(chatID, GenerateDataState.MENU);

            SendMessage sendMessage = new SendMessage(chatID, getLocalizedMessage("complete.form", userDomain.getLanguage()));
            sendMessage.replyMarkup(InlineKeyboardMarkupFactory.mainMenu(userDataRequests.get(chatID), userDomain.getLanguage()));
            bot.execute(sendMessage);
        }
    }


    private void startGenerateMockData(Long chatID, UserDomain userDomain) {
        FakerApplicationGenerateRequest request = new FakerApplicationGenerateRequest();

//        request.setFileType(FileType.JSON);
        userDataRequests.put(chatID, request);
        userState.put(chatID, GenerateDataState.GET_FILENAME);
        SendMessage sendMessage = new SendMessage(chatID, getLocalizedMessage("complete.form", userDomain.getLanguage()));
        sendMessage.replyMarkup(InlineKeyboardMarkupFactory.mainMenu(userDataRequests.get(chatID), userDomain.getLanguage()));
        bot.execute(sendMessage);

    }


    private String getValidatedFileName(String fileName) {
        if (fileName.matches("^[a-z]{3,100}$")) {
            return fileName;
        }
        return null;
    }

    private void deleteMessage(Message message, Long chatID) {
        bot.execute(new DeleteMessage(chatID, message.messageId()));
    }

    private void registration(Message message, Long chatID, RegistrationState regState, UserDomain userDomain) {
        if (regState.equals(RegistrationState.USERNAME)) {
            StringBuilder password = regState.getPassword();
            int length = password.length();
            regState.setUsername(message.text());
            String messageText = getLocalizedMessage("enter.password", "en") + "*️⃣".repeat(length) + " _ ".repeat(4 - length);
            SendMessage sendMessage = new SendMessage(chatID, messageText);
            sendMessage.replyMarkup(InlineKeyboardMarkupFactory.enterPasswordKeyboard());
            bot.execute(sendMessage);

            userState.put(chatID, RegistrationState.PASSWORD);

        } else if (regState.equals(RegistrationState.PASSWORD)) {
            System.out.println(getLocalizedMessage("password", userDomain.getLanguage()) + message.text());
            userState.put(chatID, DefaultState.DELETE);
//            SendMessage sendMessage = new SendMessage(chatID, "Successfully Registered");
//            bot.execute(sendMessage);
        }
    }

    private void startRegister(Long chatID, UserDomain userDomain) {
        userState.put(chatID, RegistrationState.USERNAME);
        RegisterUserCallbackProcessor.userRegisterPassword.put(chatID, new StringBuilder());
        SendMessage sendMessage = new SendMessage(chatID, getLocalizedMessage("welcome.message", "ne"));
        sendMessage.replyMarkup(new ForceReply());
        bot.execute(sendMessage);
    }
}
