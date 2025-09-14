package io.knowledgebase.demo.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Constant {
    SUCCESS_USER_UPDATE("User updated successfully", 0);

    private final String comment;
    private final int result;
}
