package hr.javafx.projekt.exception;

/**
 * Označena iznimka koja se baca ako korisničko ime već postoji prilikom registracije.
 */
public class UsernameAlreadyExistsException extends Exception {
  public UsernameAlreadyExistsException(String message) {
    super(message);
  }
}