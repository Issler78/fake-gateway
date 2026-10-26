package br.com.issler.fake_gateway.exceptions;

import br.com.issler.fake_gateway.contracts.GatewayResponse;
import lombok.*;

@Builder
public record ErrorResponse (
    String error,
    String message,
    int status
) implements GatewayResponse {
    public static ErrorResponse fromGatewayException(GatewayServiceException e){
        return ErrorResponse.builder()
                .error(e.getType().name())
                .message(e.getMessage())
                .status(e.getType().getStatus())
                .build();
    }
}
