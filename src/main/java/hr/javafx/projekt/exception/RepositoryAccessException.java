package hr.javafx.projekt.exception;

/**
 * Neoznačena iznimka koja se baca kod grešaka pri pristupu repozitoriju podataka.
 */
public class RepositoryAccessException extends RuntimeException {
    public RepositoryAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}