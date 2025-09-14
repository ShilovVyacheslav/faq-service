package io.knowledgebase.demo.exception;

import io.knowledgebase.demo.enums.ErrorType;
import io.knowledgebase.demo.enums.ServiceName;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class ApplicationException extends RuntimeException {

    int code;
    String message;
    HttpStatus status;
    ErrorType errorType;
    ServiceName serviceName;
    private Map<String, List<Integer>> details;

    protected ApplicationException(final String message,
                                   int code,
                                   ErrorType errorType,
                                   ServiceName serviceName,
                                   HttpStatus status) {
        super(message);
        this.code = code;
        this.message = message;
        this.status = status;
        this.errorType = errorType;
        this.serviceName = serviceName;
        this.details = Collections.emptyMap();
    }

    protected ApplicationException(final String message,
                                   int code,
                                   ErrorType errorType,
                                   ServiceName serviceName,
                                   Map<String, List<Integer>> details,
                                   HttpStatus status) {
        super(message);
        this.code = code;
        this.message = message;
        this.status = status;
        this.errorType = errorType;
        this.serviceName = serviceName;
        this.details = details;
    }

}
