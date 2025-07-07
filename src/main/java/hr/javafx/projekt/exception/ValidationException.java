package hr.javafx.projekt.exception;

/**
 * Neoznačena iznimka koja se baca kada dođe do greške pri validaciji podataka.
 */
public class ValidationException extends RuntimeException {
  public ValidationException() {
  }

  public ValidationException(String message) {
    super(message);
  }

  public ValidationException(String message, Throwable cause) {
    super(message, cause);
  }

  public ValidationException(Throwable cause) {
    super(cause);
  }

  public ValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}