package io.knowledgebase.demo.exception;

import io.knowledgebase.demo.enums.ErrorType;
import io.knowledgebase.demo.enums.ServiceName;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import static io.knowledgebase.demo.enums.ErrorCode.FAQ_DOC_NOT_FOUND_BY_ID_ERROR;
import static io.knowledgebase.demo.enums.ErrorCode.FAQ_DOES_NOT_EXIST_IN_POSTGRE_SQL;
import static io.knowledgebase.demo.enums.ErrorType.VALIDATION_ERROR;
import static io.knowledgebase.demo.enums.ServiceName.ADMIN_SERVICE;

@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FaqDocException extends ApplicationException {

    private FaqDocException(String message, int code, ErrorType errorType, ServiceName serviceName, HttpStatus status) {
        super(message, code, errorType, serviceName, status);
    }

    public static FaqDocException faqDocNotFound(Long id) {
        return new FaqDocException(
                String.format(FAQ_DOC_NOT_FOUND_BY_ID_ERROR.getMessage(), id),
                FAQ_DOC_NOT_FOUND_BY_ID_ERROR.getCode(),
                VALIDATION_ERROR,
                ADMIN_SERVICE,
                HttpStatus.NOT_FOUND
        );
    }

    public static FaqDocException faqDoesNotExistInPostgreSql(Long id) {
        return new FaqDocException(
                String.format(FAQ_DOES_NOT_EXIST_IN_POSTGRE_SQL.getMessage(), id),
                FAQ_DOES_NOT_EXIST_IN_POSTGRE_SQL.getCode(),
                VALIDATION_ERROR,
                ADMIN_SERVICE,
                HttpStatus.NOT_FOUND
        );
    }

}
