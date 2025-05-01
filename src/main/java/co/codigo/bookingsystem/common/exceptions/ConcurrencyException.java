package co.codigo.bookingsystem.common.exceptions;

public class ConcurrencyException extends RuntimeException {

    public ConcurrencyException() {
    }

    public ConcurrencyException(String message) {
        super(message);
    }

    public ConcurrencyException(Throwable cause) {
        super(cause);
    }
}
