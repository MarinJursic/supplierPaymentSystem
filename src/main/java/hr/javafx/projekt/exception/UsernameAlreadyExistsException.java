package hr.javafx.projekt.exception;

/**
 * Označena iznimka koja se baca ako korisničko ime već postoji prilikom registracije.
 */
public class UsernameAlreadyExistsException extends Exception {
  public UsernameAlreadyExistsException() {
  }

  public UsernameAlreadyExistsException(String message) {
    super(message);
  }

  public UsernameAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }

  public UsernameAlreadyExistsException(Throwable cause) {
    super(cause);
  }

  public UsernameAlreadyExistsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}