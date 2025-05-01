package co.codigo.bookingsystem.common.exceptions;

public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException() {
    }

    public BusinessRuleException(String message) {
        super(message);
    }

    public BusinessRuleException(Throwable cause) {
        super(cause);
    }
}
