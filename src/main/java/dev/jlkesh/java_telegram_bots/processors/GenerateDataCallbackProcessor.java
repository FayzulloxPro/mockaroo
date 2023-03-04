package dev.jlkesh.java_telegram_bots.processors;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Document;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.SendResponse;
import dev.jlkesh.java_telegram_bots.config.TelegramBotConfiguration;

import dev.jlkesh.java_telegram_bots.daos.FileDAO;
import dev.jlkesh.java_telegram_bots.daos.UserDao;
import dev.jlkesh.java_telegram_bots.dataserver.FakerApplicationGenerateRequest;
import dev.jlkesh.java_telegram_bots.dataserver.FakerApplicationService;
import dev.jlkesh.java_telegram_bots.dataserver.Field;
import dev.jlkesh.java_telegram_bots.dataserver.FileType;
import dev.jlkesh.java_telegram_bots.domains.FIleDomain;
import dev.jlkesh.java_telegram_bots.domains.UserDomain;
import dev.jlkesh.java_telegram_bots.dtos.FieldDTO;
import dev.jlkesh.java_telegram_bots.steps.DefaultState;
import dev.jlkesh.java_telegram_bots.steps.GenerateDataState;

import dev.jlkesh.java_telegram_bots.steps.RegistrationState;
import dev.jlkesh.java_telegram_bots.utils.AnswerCallbackQueryFactory;
import dev.jlkesh.java_telegram_bots.utils.InlineKeyboardMarkupFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import static dev.jlkesh.java_telegram_bots.config.ThreadSafeBeansContainer.*;
import static dev.jlkesh.java_telegram_bots.utils.InlineKeyboardMarkupFactory.*;
import static dev.jlkesh.java_telegram_bots.utils.MessageSourceUtils.*;

public class GenerateDataCallbackProcessor implements Processor<GenerateDataState> {
    private final TelegramBot bot = TelegramBotConfiguration.get();

    @Override
    public void process(Update update, GenerateDataState state) {

        CallbackQuery callbackQuery = update.callbackQuery();
        Message message = callbackQuery.message();
        int messageId = message.messageId();
        Long chatID = callbackQuery.message().chat().id();
        String data = callbackQuery.data();

        UserDomain userDomain=new UserDao().getUser(String.valueOf(chatID));
        String language=userDomain.getLanguage();

        if (data.equals("en")){
            UserDao dao = new UserDao();
            dao.setLanguage(chatID, data);
            bot.execute(new EditMessageText(chatID, messageId, getLocalizedMessage("language.updated", data)+getLocalizedMessage("data.generate.infomessage", data)));
            userState.put(chatID, DefaultState.NOTHING);
        }else if (data.equals("ru")){
            UserDao dao = new UserDao();
            dao.setLanguage(chatID, data);
            bot.execute(new EditMessageText(chatID, messageId, getLocalizedMessage("language.updated", data)+ getLocalizedMessage("data.generate.infomessage", data)));
            userState.put(chatID,DefaultState.NOTHING);
        } else if (data.equals("uz")) {
            UserDao dao = new UserDao();

            dao.setLanguage(chatID, data);
            bot.execute(new EditMessageText(chatID, messageId, getLocalizedMessage("language.updated", data)+getLocalizedMessage("data.generate.infomessage", data)));
            userState.put(chatID,DefaultState.NOTHING);
        }else if (data.equals("generate")) {
            FakerApplicationGenerateRequest request = userDataRequests.get(chatID);
            if (Objects.isNull(request) || !isValidRequestFields(request)) {
                bot.execute(AnswerCallbackQueryFactory.answerCallbackQuery(callbackQuery.id(), getLocalizedMessage("fill.fields", language)));
            } else {
                System.out.println();

                generateAndSendMockData(chatID, message, language);
                // TODO: 2/10/2023 generate data from request
            }
        } else if (data.equals("filename")) {
            FakerApplicationGenerateRequest request = userDataRequests.get(chatID);
            EditMessageText messageText = new EditMessageText(chatID, messageId, getLocalizedMessage("get.file.name", language));
            if (!Objects.isNull(request.getFileName())) {
                messageText.replyMarkup(InlineKeyboardMarkupFactory.backMainMenu(language));
            }
            bot.execute(messageText);
            userState.put(chatID, GenerateDataState.GET_FILENAME);
        } else if (data.equals("count")) {


            FakerApplicationGenerateRequest request = userDataRequests.get(chatID);

            userState.put(chatID, GenerateDataState.COUNT);

            EditMessageText messageText = new EditMessageText(chatID, messageId, getLocalizedMessage("get.number.rows", language));
            if (!Objects.isNull(request.getCount())) {
                messageText.replyMarkup(InlineKeyboardMarkupFactory.backMainMenu(language));
            }
            bot.execute(messageText);
        } else if (data.equals("fileType")) {

            EditMessageText markup = new EditMessageText(chatID, messageId, getLocalizedMessage("choose.file.type", language));
            markup.replyMarkup(InlineKeyboardMarkupFactory.getFileTypes(language));
            userState.put(chatID, GenerateDataState.FILE_TYPE);

            bot.execute(markup);
        } else if (data.equals("addField")) {
            userDataFields.put(String.valueOf(chatID), new FieldDTO());
            bot.execute(new EditMessageText(chatID, messageId, getLocalizedMessage("get.field.name", language)));
            userState.put(chatID, GenerateDataState.FIELD_NAME);
        } else if (data.equals("mainMenu")) {
            mainMenuCaller(messageId, chatID, language);
        } else if (data.equals("fields")) {

            fieldsListCaller(messageId, chatID, language);

        } else if (data.endsWith("_fieldInfo")) {
            String[] strings = data.split("_");
            String fieldTypeString = strings[0];
            Set<Field> fieldSet = userDataRequests.get(chatID).getFields();
            for (Field field : fieldSet) {
                if (String.valueOf(field.getFieldType()).equalsIgnoreCase(fieldTypeString)) {
                    bot.execute(new EditMessageText(chatID, messageId, getLocalizedMessage("field.name", language) + field.getFieldName() + "\n" +
                                    getLocalizedMessage("field.type", language) + field.getFieldType()).replyMarkup(
                                    InlineKeyboardMarkupFactory.fieldMenu(field, language)
                            )
                    );
                    return;
                }
            }
        } else if (data.endsWith("_deleteField")) {
            String[] strings = data.split("_");
            String stringFieldType = strings[0];
            boolean res = deleteField(stringFieldType, chatID);
            if (res) {
                bot.execute(AnswerCallbackQueryFactory.answerCallbackQuery(callbackQuery.id(), getLocalizedMessage("field.delete.success", language)));
                fieldsListCaller(messageId, chatID, language);
            } else {
                bot.execute(AnswerCallbackQueryFactory.answerCallbackQuery(callbackQuery.id(), getLocalizedMessage("somethiing.wrong", language)));
            }

        } else if (data.equals("fieldList")) {
            fieldsListCaller(messageId, chatID, language);
        } else if (data.equals("yesCSV") && state.equals(GenerateDataState.FILE_TYPE)) {
            FakerApplicationGenerateRequest request = userDataRequests.get(chatID);
            request.setFlag(true);
            request.setFileType(FileType.CSV);
            mainMenuCaller(messageId, chatID, language);

        } else if (data.equals("noCSV") && state.equals(GenerateDataState.FILE_TYPE)) {
            FakerApplicationGenerateRequest request = userDataRequests.get(chatID);
            request.setFlag(false);
            request.setFileType(FileType.CSV);
            mainMenuCaller(messageId, chatID, language);

        } else {
           if (state.equals(GenerateDataState.FILE_TYPE)) {
                FakerApplicationGenerateRequest request = userDataRequests.get(chatID);

                if (data.equals("csv")) {
                    bot.execute(new EditMessageText(chatID, messageId, getLocalizedMessage("include.header.csv", language))
                            .replyMarkup(InlineKeyboardMarkupFactory.includeHeader(language)));
                    return;
                }
                FileType fileType =
                        switch (data) {
                            case "json" -> FileType.JSON;
                            case "sql" -> FileType.SQL;
                            default -> null;
                        };
                if (!Objects.isNull(fileType)) {
                    request.setFileType(fileType);
//                    bot.execute(new EditMessageText(chatID, messageId, "File type accepted"));
                    bot.execute(new EditMessageText(chatID, messageId, getLocalizedMessage("complete.form", language)).replyMarkup(InlineKeyboardMarkupFactory.mainMenu(request, language)));
                }
            } else if (state.equals(GenerateDataState.FIELD_TYPE)) {

                getFieldType(messageId, chatID, data, language);

            } else if (state.equals(GenerateDataState.SEE_FIELD)) {
                Set<Field> fieldSet = userDataRequests.get(chatID).getFields();

            } else if (data.endsWith("/fileId")) {
                CompletableFuture.runAsync(()->{
                    bot.execute(new DeleteMessage(chatID, messageId));
                });
                String[] split = data.split("/");
                bot.execute(new SendDocument(chatID, split[0]));
            }
        }
//        if (state.equals(GenerateDataState.FIELD_TYPE)){
//
////            getFieldType(messageId, chatID, data);
//
//        } else if (state.equals(GenerateDataState.MINMAX)) {
////
////            if (data.equals("default")){
////
////                setMinMax(messageId, chatID);
////
////            }
//            // TODO: 2/5/2023 add min or max values manually
//
//        } else if (state.equals(GenerateDataState.ADD_OR_GENERATE)) {
//
//
////            bot.execute(new DeleteMessage(chatID, messageId));
////
////            if (data.equals("add")){
////
////                userState.put(chatID, GenerateDataState.FIELD_NAME);
////                bot.execute(new SendMessage(chatID, "Enter the field name: "));
////
////            } else if ("generate".equals(data)) {
////
////                generateAndSendMockData(chatID);
////            }
//        }el

    }

    private void fieldsListCaller(int messageId, Long chatID, String language) {
        Set<Field> fieldSet = userDataRequests.get(chatID).getFields();
        bot.execute(new EditMessageText(chatID, messageId, getLocalizedMessage("your.fields", language)).replyMarkup(InlineKeyboardMarkupFactory.getFields(fieldSet, language)));
    }

    private void mainMenuCaller(int messageId, Long chatID, String language) {
        bot.execute(
                new EditMessageText(chatID, messageId, getLocalizedMessage("complete.form", language))
                        .replyMarkup(
                                InlineKeyboardMarkupFactory.mainMenu(userDataRequests.get(chatID), language)
                        )
        );
    }

    private boolean deleteField(String stringFieldType, Long chatID) {
        Set<Field> set = userDataRequests.get(chatID).getFields();
        for (Field field : set) {
            if (String.valueOf(field.getFieldType()).equalsIgnoreCase(stringFieldType)) {
                set.remove(field);
                return true;
            }
        }
        return false;
    }

    private boolean isValidRequestFields(FakerApplicationGenerateRequest request) {

        return !Objects.isNull(request.getFileName()) &&
                !Objects.isNull(request.getFields()) &&
                !Objects.isNull(request.getCount()) &&
                !Objects.isNull(request.getFileType()) &&
                request.getFields().size() != 0;
    }

    private void generateAndSendMockData(Long chatID, Message message, String language) {
        int messageId = message.messageId();
        FakerApplicationService service = new FakerApplicationService();

        FakerApplicationGenerateRequest request = userDataRequests.get(chatID);

        File file = service.processRequest(request);

        bot.execute(new SendMessage(chatID, getLocalizedMessage("file.sending", language)));
        SendDocument document = new SendDocument(chatID, file);
        SendResponse response = bot.execute(document);

        Document doc = response.message().document();
        CompletableFuture.runAsync(() -> {
            executor.submit(() -> {
                        if (file.exists()) {
                            boolean delete = file.delete();
                        }
                    }
            );
        });

        bot.execute(new DeleteMessage(chatID, messageId));

        CompletableFuture.runAsync(()->{
            FileDAO fileDAO = FileDAO.getInstance();

            FIleDomain fIleDomain=FIleDomain.builder()
                    .chatId(String.valueOf(chatID))
                    .fileId(doc.fileId())
                    .fileName(document.getFileName())
                    .rowsCount(request.getCount())
                    .build();
            fileDAO.save(fIleDomain);
        });
        userDataRequests.remove(chatID);
        userState.put(chatID, GenerateDataState.MENU);
    }

    private void getFieldType(int messageId, Long chatID, String data, String language) {
        FieldDTO fieldDTO = userDataFields.get(String.valueOf(chatID));
        fieldDTO.setFieldType(fields.get(Integer.parseInt(data)));
        FakerApplicationGenerateRequest request = userDataRequests.get(chatID);
        request.getFields().add(
                new Field(
                        fieldDTO.getFieldName(),
                        fieldDTO.getFieldType(),
                        0, 100)
        );
        userState.put(chatID, GenerateDataState.MENU);
        bot.execute(new EditMessageText(chatID, messageId, getLocalizedMessage("complete.form", language)).replyMarkup(InlineKeyboardMarkupFactory.mainMenu(request, language)));
    }

//    private void setMinMax(int messageId, Long chatID, String language) {
//        bot.execute(new DeleteMessage(chatID, messageId));
//        FieldDTO fieldDTO = userDataFields.get(String.valueOf(chatID));
//        FakerApplicationGenerateRequest request = userDataRequests.get(chatID);
//        Set<Field> fieldSet = request.getFields();
//        fieldSet.add(new Field(
//                fieldDTO.getFieldName(),
//                fieldDTO.getFieldType(),
//                fieldDTO.getMin(),
//                fieldDTO.getMax()
//        ));
//        request.setFields(fieldSet);
//
//        userState.put(chatID, GenerateDataState.ADD_OR_GENERATE);
//
//        SendMessage sendMessage = new SendMessage(chatID, getLocalizedMessage("want.add.types", language));
//        sendMessage.replyMarkup(InlineKeyboardMarkupFactory.addOrGenerateData());
//
//        bot.execute(sendMessage);
//    }
}
