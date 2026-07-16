package com.workspace.dto;

public record ResponseDTO<T>(
        boolean success,
        String message,
        T body
) {
}
