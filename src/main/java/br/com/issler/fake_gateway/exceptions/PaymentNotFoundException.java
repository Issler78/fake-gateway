package br.com.issler.fake_gateway.exceptions;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String message) { super(message); }
}
