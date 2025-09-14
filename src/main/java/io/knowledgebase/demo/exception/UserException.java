package io.knowledgebase.demo.exception;

import io.knowledgebase.demo.enums.ErrorType;
import io.knowledgebase.demo.enums.ServiceName;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import static io.knowledgebase.demo.enums.ErrorCode.USER_ALREADY_EXIST_ERROR;
import static io.knowledgebase.demo.enums.ErrorCode.USER_NOT_FOUND_BY_ID_ERROR;
import static io.knowledgebase.demo.enums.ErrorCode.USER_NOT_FOUND_BY_USERNAME_ERROR;
import static io.knowledgebase.demo.enums.ErrorType.VALIDATION_ERROR;
import static io.knowledgebase.demo.enums.ServiceName.ADMIN_SERVICE;

@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserException extends ApplicationException {

    private UserException(String message, int code, ErrorType errorType, ServiceName serviceName, HttpStatus status) {
        super(message, code, errorType, serviceName, status);
    }

    public static UserException userAlreadyExists(String identifier) {
        return new UserException(
                String.format(USER_ALREADY_EXIST_ERROR.getMessage(), identifier),
                USER_ALREADY_EXIST_ERROR.getCode(),
                VALIDATION_ERROR,
                ADMIN_SERVICE,
                HttpStatus.CONFLICT
        );
    }

    public static UserException userNotFound(Long id) {
        return new UserException(
                String.format(USER_NOT_FOUND_BY_ID_ERROR.getMessage(), id),
                USER_NOT_FOUND_BY_ID_ERROR.getCode(),
                VALIDATION_ERROR,
                ADMIN_SERVICE,
                HttpStatus.NOT_FOUND
        );
    }

    public static UserException userNotFound(String username) {
        return new UserException(
                String.format(USER_NOT_FOUND_BY_USERNAME_ERROR.getMessage(), username),
                USER_NOT_FOUND_BY_USERNAME_ERROR.getCode(),
                VALIDATION_ERROR,
                ADMIN_SERVICE,
                HttpStatus.NOT_FOUND
        );
    }

}
