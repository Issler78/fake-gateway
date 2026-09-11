package br.com.issler.fake_gateway.exceptions;

import lombok.*;

@Builder
public record ErrorResponse (
    String error,
    String message,
    int status
) {}
