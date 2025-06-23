package hr.javafx.projekt.controller;

import hr.javafx.projekt.enums.UserRole;
import hr.javafx.projekt.exception.InvalidLoginException;
import hr.javafx.projekt.repository.UserRepository;
import hr.javafx.projekt.session.SessionManager;
import hr.javafx.projekt.utils.Navigation;
import hr.javafx.projekt.generics.Pair;
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

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final UserRepository userRepository = new UserRepository();
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            Optional<Pair<Long, UserRole>> userOptional = userRepository.authenticate(username, password);
            userOptional.ifPresent(user -> {
                SessionManager.login(user.getKey(), user.getValue());
                Navigation.showScene("dashboard.fxml", "Dashboard - Supplier Payment System");
            });
        } catch (InvalidLoginException e) {
            errorLabel.setText(e.getMessage());
            log.warn("Neuspješan pokušaj prijave za korisnika: {}", username);
        } catch (Exception e) {
            errorLabel.setText("Dogodila se sistemska greška.");
            log.error("Sistemska greška pri prijavi.", e);
        }
    }

    @FXML
    private void showRegistrationScreen() {
        Navigation.showScene("registration.fxml", "Registracija");
    }
}