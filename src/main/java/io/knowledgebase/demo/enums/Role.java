package io.knowledgebase.demo.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {
    USER,
    EXPERT,
    ADMIN,
    AGENT;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static Role fromString(String value) {
        return Role.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toLower() {
        return name().toLowerCase();
    }
}
