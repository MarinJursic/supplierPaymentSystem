package hr.javafx.projekt.exception;

/**
 * Označena iznimka koja se baca prilikom neuspješnog pokušaja prijave.
 */
public class InvalidLoginException extends Exception {
    public InvalidLoginException(String message) {
        super(message);
    }

    public InvalidLoginException(String message, Throwable cause) {
        super(message, cause);
    }
}