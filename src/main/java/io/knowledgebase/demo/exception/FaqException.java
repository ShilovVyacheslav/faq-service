package io.knowledgebase.demo.exception;

import io.knowledgebase.demo.enums.ErrorCode;
import io.knowledgebase.demo.enums.ErrorType;
import io.knowledgebase.demo.enums.ServiceName;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import static io.knowledgebase.demo.enums.ErrorCode.FAQ_ALREADY_EXISTS;

@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FaqException extends ApplicationException {

    private FaqException(String message, int code, ErrorType errorType, ServiceName serviceName, HttpStatus status) {
        super(message, code, errorType, serviceName, status);
    }

    public static FaqException faqNotFound(Long id) {
        return new FaqException(
                String.format("FAQ not found with id: " + id),
                ErrorCode.FAQ_NOT_FOUND.getCode(),
                ErrorType.VALIDATION_ERROR,
                ServiceName.ADMIN_SERVICE,
                HttpStatus.NOT_FOUND
        );
    }

    public static FaqException faqAlreadyExists(String question, String answer) {
        return new FaqException(
                String.format(FAQ_ALREADY_EXISTS.getMessage(), question, answer),
                FAQ_ALREADY_EXISTS.getCode(),
                ErrorType.INTERNAL_ERROR,
                ServiceName.ADMIN_SERVICE,
                HttpStatus.CONFLICT
        );
    }

}
