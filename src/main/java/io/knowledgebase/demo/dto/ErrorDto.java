package io.knowledgebase.demo.dto;

import io.knowledgebase.demo.enums.ErrorType;
import io.knowledgebase.demo.enums.ServiceName;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record ErrorDto(Integer code, String message, ErrorType errorType, ServiceName serviceName, Map<String, List<Integer>> details) {
    public ErrorDto(Integer code, String message, ErrorType errorType, ServiceName serviceName) {
        this(code, message, errorType, serviceName, Collections.emptyMap());
    }

    public ErrorDto(Integer code, String message, ErrorType errorType, ServiceName serviceName, Map<String, List<Integer>> details) {
        this.code = code;
        this.message = message;
        this.errorType = errorType;
        this.serviceName = serviceName;
        this.details = details;
    }

    public Integer code() {
        return this.code;
    }

    public String message() {
        return this.message;
    }

    public ErrorType errorType() {
        return this.errorType;
    }

    public ServiceName serviceName() {
        return this.serviceName;
    }

    public Map<String, List<Integer>> details() {
        return this.details;
    }
}
