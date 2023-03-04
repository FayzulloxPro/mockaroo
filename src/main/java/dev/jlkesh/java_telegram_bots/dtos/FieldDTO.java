package dev.jlkesh.java_telegram_bots.dtos;

import dev.jlkesh.java_telegram_bots.dataserver.FieldType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.function.BiFunction;

@Getter
@Setter

public class FieldDTO {
    private String fieldName;
    private FieldType fieldType;
    private BiFunction<Integer, Integer, Object> func;
    private int min=0;
    private int max=100;
}
