package dev.jlkesh.java_telegram_bots.dataserver;

import lombok.Getter;
import lombok.Setter;

public enum FieldType {
    ID(""),
    UUID("\""),
    BOOK_TITLE("\""),
    BOOT_AUTHOR("\""),
    POST_TITLE("\""),
    POST_BODY("\""),
    FIRSTNAME("\""),
    LASTNAME("\""),
    USERNAME("\""),
    FULLNAME("\""),
    BLOOD_GROUP("\""),
    EMAIL("\""),
    GENDER("\""),
    PHONE("\""),
    LOCAlDATE("\""),
    AGE(""),
    COUNTRY_CODE("\""),
    COUNTRY_ZIP_CODE("\""),
    CAPITAL("\""),
    WORD("\""),
    WORDS("\""),
    PARAGRAPH("\""),
    PARAGRAPHS("\""),
    LETTERS("\""),
    RANDOM_INT("");

    private final String i;

    FieldType(String i) {
        this.i = i;
    }


    public String getRowAsJson(String fieldName, Object data) {
        return ("\"" + fieldName + "\" : " + i + data + i);
    }

    public String getRowAsCSV(String fieldName, Object data) {
        return ("" + i + data + i);
    }

    public SQLData getRowAsSQL(String fieldName, Object data) {
        SQLData sqlData = new SQLData();
        if (!FakerApplicationService.NUMERIC_FIELDS.contains(fieldName.toUpperCase())){
            sqlData.setValue("\""+data.toString()+"\"");
        }else {
            sqlData.setValue(data.toString());
        }
        sqlData.setFieldName(fieldName);
        return sqlData;
    }

    @Getter
    @Setter

    static class SQLData {
        String fieldName;
        String value;
    }
}
