package br.com.issler.fake_gateway.contracts;

import br.com.issler.fake_gateway.dtos.PaymentResponse;
import br.com.issler.fake_gateway.exceptions.ErrorResponse;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        property = "type",
        include = JsonTypeInfo.As.PROPERTY,
        use = JsonTypeInfo.Id.NAME
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PaymentResponse.class, name = "success"),
        @JsonSubTypes.Type(value = ErrorResponse.class, name = "error")
})
public interface GatewayResponse {
}
