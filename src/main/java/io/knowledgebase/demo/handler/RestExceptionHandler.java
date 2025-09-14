package io.knowledgebase.demo.handler;

import io.knowledgebase.demo.dto.ErrorDto;
import io.knowledgebase.demo.exception.ApplicationException;
import io.knowledgebase.demo.exception.AuthException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static io.knowledgebase.demo.enums.ErrorType.INTERNAL_ERROR;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Log4j2
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleException(final Exception ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof ApplicationException) {
            return handleApplicationException((ApplicationException) cause);
        }

        log.error("Exception : {}", ex.getMessage(), ex);

        var error = new ErrorDto(
                INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                INTERNAL_ERROR,
                null,
                null);

        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(error);
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorDto> handleApplicationException(final ApplicationException ex) {
        log.warn("ApplicationException [{}]: - {}", ex.getClass().getSimpleName(), ex.getMessage());

        var error = new ErrorDto(
                ex.getCode(),
                ex.getMessage(),
                ex.getErrorType(),
                ex.getServiceName(),
                ex.getDetails()
        );

        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDto> handleAccessDeniedException(final AccessDeniedException accessDeniedException) {
        return handleApplicationException(AuthException.accessDenied());
    }

}
