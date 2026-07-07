package br.com.issler.fake_gateway.exceptions;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class ErrorResponse {
    String message;
    int status;
}
