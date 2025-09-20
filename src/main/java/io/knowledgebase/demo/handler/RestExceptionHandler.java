package io.knowledgebase.demo.handler;

import io.knowledgebase.demo.dto.ErrorDto;
import io.knowledgebase.demo.exception.ApplicationException;
import io.knowledgebase.demo.exception.AuthException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static io.knowledgebase.demo.enums.ErrorCode.INVALID_REQUEST_PARAM_ERROR_CODE;
import static io.knowledgebase.demo.enums.ErrorCode.VALIDATION_FAILED_ERROR_CODE;
import static io.knowledgebase.demo.enums.ErrorType.INTERNAL_ERROR;
import static io.knowledgebase.demo.enums.ErrorType.VALIDATION_ERROR;
import static io.knowledgebase.demo.enums.ServiceName.ADMIN_SERVICE;
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

    @NonNull
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        log.error("MethodArgumentNotValidException = {}", ex.getMessage());

        Map<String, List<Integer>> details = handleFieldErrors(ex.getFieldErrors());
        var error = new ErrorDto(
                VALIDATION_FAILED_ERROR_CODE.getCode(),
                VALIDATION_FAILED_ERROR_CODE.getMessage(),
                VALIDATION_ERROR,
                ADMIN_SERVICE,
                details);

        return ResponseEntity
                .status(status)
                .body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDto> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("ConstraintViolationException = {}", ex.getMessage());

        Map<String, List<Integer>> details = handleFieldErrors(ex.getConstraintViolations());
        var error = new ErrorDto(
                VALIDATION_FAILED_ERROR_CODE.getCode(),
                VALIDATION_FAILED_ERROR_CODE.getMessage(),
                VALIDATION_ERROR,
                ADMIN_SERVICE,
                details);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    private static Map<String, List<Integer>> handleFieldErrors(List<FieldError> errors) {

        return errors.stream()
                .collect(Collectors.groupingBy(FieldError::getField,
                        Collectors.mapping(
                                field -> getErrorCodeOrDefault(field.getDefaultMessage(),
                                        INVALID_REQUEST_PARAM_ERROR_CODE.getCode()),
                                Collectors.toList()
                        )
                ));
    }

    private static Integer getErrorCodeOrDefault(String message, Integer defaultCode) {
        try {
            String code = message.substring(message.length() - 6);
            return Integer.valueOf(code);
        } catch (Exception var3) {
            return defaultCode;
        }
    }

    private static Map<String, List<Integer>> handleFieldErrors(Set<ConstraintViolation<?>> violations) {

        return violations.stream()
                .collect(Collectors.groupingBy(fieldError -> {
                    var field = StreamSupport.stream(fieldError.getPropertyPath().spliterator(), false)
                            .reduce((node, node2) -> node2)
                            .map(Path.Node::toString)
                            .orElse("");
                    return field.isBlank() ? "general_errors" : field;
                }, Collectors.mapping(
                        fieldError -> getErrorCodeOrDefault(fieldError.getMessage(), INVALID_REQUEST_PARAM_ERROR_CODE.getCode()),
                        Collectors.toList())));
    }

}
