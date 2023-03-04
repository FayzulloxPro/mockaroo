package dev.jlkesh.java_telegram_bots.steps;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GenerateDataState implements State {
    GET_FILENAME,
    COUNT,
    FIELD_TYPE,

    FIELD_NAME,
    SET_MIN,
    SET_MAX,
    MINMAX,
    ADD_OR_GENERATE,
    FILE_TYPE, SEE_FIELD, MENU,
    CSV_HEADER
}
