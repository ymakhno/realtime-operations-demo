package com.demo.to;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FieldType {
    INTEGER, STRING, TIMESTAMP, BOOLEAN;

    @JsonValue
    public String lowerCase() {
        return this.name().toLowerCase();
    }

    @JsonCreator
    public static FieldType valueOfIgnoreCase(String string) {
        return valueOf(string.toUpperCase());
    }
}
