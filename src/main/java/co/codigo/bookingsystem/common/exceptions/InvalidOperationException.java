package co.codigo.bookingsystem.common.exceptions;

public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException() {
    }

    public InvalidOperationException(String message) {
        super(message);
    }

    public InvalidOperationException(Throwable cause) {
        super(cause);
    }
}
