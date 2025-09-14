package io.knowledgebase.demo.exception;

import io.knowledgebase.demo.enums.ErrorType;
import io.knowledgebase.demo.enums.ServiceName;
import org.springframework.http.HttpStatus;

import static io.knowledgebase.demo.enums.ErrorCode.FAQ_INVALID_SORT_FIELD;
import static io.knowledgebase.demo.enums.ErrorType.VALIDATION_ERROR;
import static io.knowledgebase.demo.enums.ServiceName.ADMIN_SERVICE;

public class InvalidSortFieldException extends ApplicationException {

    private InvalidSortFieldException(String message, int code, ErrorType errorType, ServiceName serviceName, HttpStatus status) {
        super(message, code, errorType, serviceName, status);
    }

    public static InvalidSortFieldException invalidSortFieldException(String invalidSortField) {
        return new InvalidSortFieldException(
                String.format(FAQ_INVALID_SORT_FIELD.getMessage(), invalidSortField),
                FAQ_INVALID_SORT_FIELD.getCode(),
                VALIDATION_ERROR,
                ADMIN_SERVICE,
                HttpStatus.BAD_REQUEST
        );
    }

}
