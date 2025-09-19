package io.knowledgebase.demo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_CREDENTIALS_ERROR("Invalid username or password", 401),
    ACCESS_DENIED_ERROR("Access denied", 403),
    JWT_AUTH_ERROR("Authentication failed due to invalid or expired token", 401),
    JWT_SYSTEM_ERROR("Internal authentication system error", 500),

    USER_ALREADY_EXIST_ERROR("User '%s' already exists", 409),
    USER_NOT_FOUND_BY_ID_ERROR("User not found with id: %s", 404),
    USER_NOT_FOUND_BY_USERNAME_ERROR("User not found with username: %s", 404),

    FAQ_INVALID_SORT_FIELD("Sorting by field '%s' is not allowed", 400),
    FAQ_NOT_FOUND("FAQ not found with id: %s", 404),
    FAQ_ALREADY_EXISTS("FAQ with question '%s' and answer '%s' already exists", 409),
    FAQ_DOC_NOT_FOUND_BY_ID_ERROR("FaqDoc not found with id: %s", 404),
    FAQ_DOES_NOT_EXIST_IN_POSTGRE_SQL("FAQ with id %s doesn't exist in PostgreSQL", 404),

    VALIDATION_FAILED_ERROR_CODE("validation failed", 100556),
    INVALID_REQUEST_PARAM_ERROR_CODE("invalid request param", 100010);

    private final String message;
    private final int code;
}
