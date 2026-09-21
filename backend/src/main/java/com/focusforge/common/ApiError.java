package com.focusforge.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(Instant timestamp, int status, String message, String path, Map<String, String> fieldErrors) {
    public static ApiError of(int status, String message, String path) {
        return new ApiError(Instant.now(), status, message, path, null);
    }
}
