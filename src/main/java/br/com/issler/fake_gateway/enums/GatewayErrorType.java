package br.com.issler.fake_gateway.enums;

import lombok.Getter;

@Getter
public enum GatewayErrorType {
    INTERNAL_FAILURE(500, false),
    RATE_LIMITED(429, true),
    INVALID_DOCUMENT(400, true);

    private final int status;
    private final boolean clientError;

    GatewayErrorType(int status, boolean clientError) {
        this.status = status;
        this.clientError = clientError;
    }
}
