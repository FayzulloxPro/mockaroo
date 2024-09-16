package dev.jlkesh.java_telegram_bots.utils;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import dev.jlkesh.java_telegram_bots.daos.FileDAO;
import dev.jlkesh.java_telegram_bots.dataserver.FakerApplicationGenerateRequest;
import dev.jlkesh.java_telegram_bots.dataserver.Field;
import dev.jlkesh.java_telegram_bots.dataserver.FieldType;
import dev.jlkesh.java_telegram_bots.domains.FIleDomain;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import static dev.jlkesh.java_telegram_bots.utils.MessageSourceUtils.*;

public class InlineKeyboardMarkupFactory {
    public static final List<FieldType> fields = Collections.synchronizedList(new ArrayList<>());
    private static final int fieldsCount;

    static {
//        fields.add(null);
        fields.addAll(Arrays.asList(FieldType.values()));
        fieldsCount = fields.size();
    }

    private static InlineKeyboardMarkup mainMenu(String lang) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        markup.addRow(
                getInlineButton(getLocalizedMessage("b.set.file.name", lang), "filename"),
                getInlineButton("number of rows", "count")
        );
        markup.addRow(
                getInlineButton("set file type", "fileType"),
                getInlineButton("add field", "addField")
        );
        markup.addRow(
                getInlineButton("\uD83C\uDFB2 Generate data", "generate")
        );
        return markup;
    }

    public static InlineKeyboardMarkup mainMenu(FakerApplicationGenerateRequest request, String lang) {
        if (Objects.isNull(request)) {
            return mainMenu(lang);
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.addRow(
                getInlineButton(
                        (Objects.isNull(request.getFileName()) ? getLocalizedMessage("b.set.file.name", lang) : "✅"+getLocalizedMessage("b.file.name", lang) + request.getFileName()),
                        "filename"),

                getInlineButton(
                        (Objects.isNull(request.getCount()) ? getLocalizedMessage("b.set.number.rows", lang) : "✅"+getLocalizedMessage("b.number.rows", lang) + request.getCount()),
                        "count")
        );
        if (!Objects.isNull(request.getFields()) && request.getFields().size() != 0) {
            markup.addRow(
                    getInlineButton("✅"+getLocalizedMessage("b.added.fields", lang) + request.getFields().size(), "fields")
            );
        }
        markup.addRow(
                getInlineButton((Objects.isNull(request.getFileType()) ? getLocalizedMessage("b.set.file.type", lang) : "✅ "+getLocalizedMessage("b.file.type", lang) + request.getFileType()), "fileType"),
                getInlineButton(getLocalizedMessage("b.add.field", lang), "addField")
        );
        markup.addRow(
                getInlineButton(getLocalizedMessage("b.generate.data", lang), "generate")
        );

        return markup;
    }

    public static InlineKeyboardMarkup enterPasswordKeyboard() {
        InlineKeyboardMarkup replyMarkup = new InlineKeyboardMarkup();
        replyMarkup.addRow(
                getInlineButton("show password", "show"),
                getInlineButton("hide password", "hide")
        );
        replyMarkup.addRow(
                getInlineButton(1, 1),
                getInlineButton(2, 2),
                getInlineButton(3, 3)
        );
        replyMarkup.addRow(
                getInlineButton(4, 4),
                getInlineButton(5, 5),
                getInlineButton(6, 6)
        );
        replyMarkup.addRow(
                getInlineButton(7, 7),
                getInlineButton(8, 8),
                getInlineButton(9, 9)
        );
        replyMarkup.addRow(
                getInlineButton(0, 0),
                getInlineButton("✅", "done"),
                getInlineButton("⬅️", "d")
        );

        return replyMarkup;
    }

    private static InlineKeyboardButton getInlineButton(final Object text, final Object callbackData) {
        var button = new InlineKeyboardButton(Objects.toString(text));
        button.callbackData(Objects.toString(callbackData));
        return button;
    }

    public static InlineKeyboardMarkup getFieldTypes(String lang) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        switch (fields.size() % 3) {
            case 0 -> {
                for (int i = 0; i < fields.size(); i = i + 3) {
                    markup.addRow(
                            getInlineButton(fields.get(i).toString(), i),
                            getInlineButton(fields.get(i + 1).toString(), (i + 1)),
                            getInlineButton(fields.get(i + 2).toString(), (i + 2))
                    );
                }
            }
            case 1 -> {
                for (int i = 0; i < fields.size() - 1; i = i + 3) {
                    markup.addRow(
                            getInlineButton(fields.get(i).toString(), i),
                            getInlineButton(fields.get(i + 1).toString(), (i + 1)),
                            getInlineButton(fields.get(i + 2).toString(), (i + 2))
                    );
                }
                markup.addRow(getInlineButton(fields.get(fields.size() - 1), (fields.size() - 1)));
            }
            case 2 -> {
                for (int i = 0; i < fields.size() - 2; i = i + 3) {
                    markup.addRow(
                            getInlineButton(fields.get(i).toString(), i),
                            getInlineButton(fields.get(i + 1).toString(), (i + 1)),
                            getInlineButton(fields.get(i + 2).toString(), (i + 2))
                    );
                }
                markup.addRow(getInlineButton(fields.get(fields.size() - 2), (fields.size() - 2)),
                        getInlineButton(fields.get(fields.size() - 1), (fields.size() - 1))
                );
            }
        }

        return markup.addRow(getInlineButton(getLocalizedMessage("b.back", lang), "mainMenu"));
    }

    private static void showFieldTypes() {
        int i = 1;
        for (FieldType fieldType : FieldType.values()) {
            System.out.printf("%2d.%-20s", i, fieldType);
            if (i % 2 == 0)
                System.out.println();
            i++;
        }
    }

    public static InlineKeyboardMarkup setMinMaxValues() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();


        markup.addRow(
                getInlineButton("set min value", "min"),
                getInlineButton("set default values", "default"),
                getInlineButton("set max value", "max")
        );
        return markup;
    }

    public static InlineKeyboardMarkup includeHeader(String lang) {
        return new InlineKeyboardMarkup().addRow(
                getInlineButton(getLocalizedMessage("b.no", lang), "noCSV"),
                getInlineButton(getLocalizedMessage("b.yes", lang), "yesCSV")
        ).addRow(
        getInlineButton(getLocalizedMessage("b.back", lang), "mainMenu")
        );
    }

//    public static InlineKeyboardMarkup addOrGenerateData() {
//        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
//        markup.addRow(
//                getInlineButton("Add again", "add"),
//                getInlineButton("No, generate my data", "generate")
//        );
//
//        return markup;
//    }

    public static InlineKeyboardMarkup getFileTypes(String lang) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.addRow(
                getInlineButton("JSON", "json"),
                getInlineButton("CSV", "csv"),
                getInlineButton("SQL", "sql")
        );
        markup.addRow(getInlineButton(getLocalizedMessage("b.back", lang), "mainMenu"));
        return markup;
    }

    public static InlineKeyboardMarkup backMainMenu(String lang) {
        return new InlineKeyboardMarkup().addRow(getInlineButton(getLocalizedMessage("b.back", lang), "mainMenu"));
    }

    public static InlineKeyboardMarkup getFields(Set<Field> fieldSet, String lang) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        for (Field field : fieldSet) {
            markup.addRow(
                    getInlineButton(field.getFieldName(), field.getFieldType() + "_fieldInfo")
            );
        }
        return markup.addRow(getInlineButton(getLocalizedMessage("b.back", lang), "mainMenu"));
    }

    public static InlineKeyboardMarkup fieldMenu(Field field, String lang) {
        return new InlineKeyboardMarkup().addRow(
                getInlineButton(getLocalizedMessage("b.back.field.list",lang), "fieldList"),
                getInlineButton(getLocalizedMessage("b.delete.field", lang), field.getFieldType() + "_deleteField"),
                getInlineButton("⬆️"+getLocalizedMessage("b.main.menu", lang), "mainMenu")
        );
    }

    public static InlineKeyboardMarkup getHistory(int page, String chatId, List<FIleDomain> files) {

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();


        if (files.size() % 2 == 0) {
            for (int i = 0; i < files.size(); i = i + 2) {
                markup.addRow(
                        getInlineButton(
                                (i + 1), files.get(i).getFileId() + "/fileId"
                        ),
                        getInlineButton((i + 2), files.get(i + 1).getFileId() + "/fileId")
                );
            }
        } else {
            for (int i = 0; i < files.size() ; i = i + 2) {
                markup.addRow(
                        getInlineButton((i + 1), files.get(i).getFileId() + "/fileId"),
                        getInlineButton((i + 2), files.get(i + 1).getFileId() + "/fileId")
                );
            }
            markup.addRow(
                    getInlineButton(files.size() ,files.get(files.size()-1)+"/fileId")
            );
        }

        return markup;
    }

    public static InlineKeyboardMarkup languages(String language) {
        InlineKeyboardMarkup markup=new InlineKeyboardMarkup();

        markup.addRow(
                getInlineButton(getLocalizedMessage("b.lang.eng", language), "en"),
                getInlineButton(getLocalizedMessage("b.lang.ru", language), "ru"),
                getInlineButton(getLocalizedMessage("b.lang.uz", language), "uz")
        );
        markup.addRow(getInlineButton("❌", "del"));
        return markup;
    }
}
