package hr.javafx.projekt.exception;

/**
 * Neoznačena iznimka koja se baca kada dođe do općenite greške
 * prilikom pristupa repozitoriju, najčešće vezane uz I/O operacije ili probleme s bazom podataka.
 */
public class RepositoryAccessException extends RuntimeException {
    public RepositoryAccessException() {
        super();
    }

    public RepositoryAccessException(String message) {
        super(message);
    }

    public RepositoryAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryAccessException(Throwable cause) {
        super(cause);
    }

    public RepositoryAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}