package hr.javafx.projekt.controller;

import hr.javafx.projekt.enums.UserRole;
import hr.javafx.projekt.exception.InvalidLoginException;
import hr.javafx.projekt.exception.RepositoryAccessException;
import hr.javafx.projekt.repository.Pair;
import hr.javafx.projekt.repository.UserRepository;
import hr.javafx.projekt.session.SessionManager;
import hr.javafx.projekt.utils.Navigation;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Kontroler za ekran za prijavu korisnika.
 */
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final UserRepository userRepository = new UserRepository();

    /**
     * Rukuje pokušajem prijave korisnika.
     * Provjerava unesene podatke i, ako su ispravni, pokreće sesiju i preusmjerava na dashboard.
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isBlank() || password.isEmpty()) {
            errorLabel.setText("Korisničko ime i lozinka su obavezni.");
            return;
        }

        errorLabel.setText("");

        try {
            Optional<Pair<Long, UserRole>> userOptional = userRepository.authenticate(username, password);

            if (userOptional.isPresent()) {
                Pair<Long, UserRole> user = userOptional.get();
                SessionManager.login(user.getKey(), user.getValue());
                Navigation.showScene("dashboard.fxml", "Dashboard - Supplier Payment System");
            } else {
                errorLabel.setText("Neispravno korisničko ime ili lozinka.");
                log.warn("Neuspješan pokušaj prijave za korisnika: {}", username);
            }
        } catch (InvalidLoginException e) {
            errorLabel.setText(e.getMessage());
            log.warn("Neuspješan pokušaj prijave za korisnika: {}", username, e);
        } catch (RepositoryAccessException e) {
            errorLabel.setText("Greška pri pristupu podacima. Pokušajte ponovno.");
            log.error("Greška repozitorija prilikom prijave.", e);
        }
    }

    /**
     * Prikazuje ekran za registraciju novog korisnika.
     */
    @FXML
    private void showRegistrationScreen() {
        Navigation.showScene("registration.fxml", "Registracija");
    }
}