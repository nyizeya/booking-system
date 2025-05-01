package co.codigo.bookingsystem.common.exceptions;

public class InsufficientCreditsException extends RuntimeException {

    public InsufficientCreditsException() {
    }

    public InsufficientCreditsException(String message) {
        super(message);
    }

    public InsufficientCreditsException(Throwable cause) {
        super(cause);
    }
}
