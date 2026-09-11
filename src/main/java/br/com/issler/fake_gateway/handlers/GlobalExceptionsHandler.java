package br.com.issler.fake_gateway.handlers;

import br.com.issler.fake_gateway.exceptions.ErrorResponse;
import br.com.issler.fake_gateway.exceptions.PaymentNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.InvalidFormatException;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionsHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        ErrorResponse errorResponse = ErrorResponse.builder()
                // field error + message error
                .message(Objects.requireNonNull(e.getBindingResult().getFieldError()).getField() + " " + Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage())
                .error("VALIDATION_ERROR")
                .status(HttpStatus.BAD_REQUEST.value())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJsonException(HttpMessageNotReadableException e){
        if (e.getMostSpecificCause() instanceof InvalidFormatException invalid &&
                invalid.getTargetType().isEnum())
        {

            String field = invalid.getPath().getFirst().getPropertyName();
            String allowedValues = Arrays.stream(invalid.getTargetType().getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));



            ErrorResponse errorResponse = ErrorResponse.builder()
                    .message(field + " should be either: " + allowedValues)
                    .error("VALIDATION_ERROR")
                    .status(HttpStatus.BAD_REQUEST.value())
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Invalid JSON")
                .error("VALIDATION_ERROR")
                .status(HttpStatus.BAD_REQUEST.value())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFoundException(PaymentNotFoundException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(e.getMessage())
                .error("PAYMENT_NOT_FOUND")
                .status(HttpStatus.NOT_FOUND.value())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e){

        ErrorResponse response = ErrorResponse.builder()
                .message("Internal server error")
                .error("INTERNAL_ERROR")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
