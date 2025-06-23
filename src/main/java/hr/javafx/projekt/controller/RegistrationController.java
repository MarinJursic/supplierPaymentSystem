package hr.javafx.projekt.controller;

import hr.javafx.projekt.enums.UserRole;
import hr.javafx.projekt.exception.UsernameAlreadyExistsException;
import hr.javafx.projekt.repository.UserRepository;
import hr.javafx.projekt.utils.Navigation;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kontroler za ekran za registraciju korisnika.
 */
public class RegistrationController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<UserRole> roleComboBox; // Novi FXML element
    @FXML private Label errorLabel;

    private final UserRepository userRepository = new UserRepository();
    private static final Logger log = LoggerFactory.getLogger(RegistrationController.class);

    /**
     * Inicijalizira kontroler, popunjava ComboBox s rolama.
     */
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList(UserRole.values()));
        roleComboBox.getSelectionModel().select(UserRole.USER); // Postavlja defaultnu vrijednost
    }

    /**
     * Rukuje događajem klika na gumb za registraciju.
     */
    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        UserRole selectedRole = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || selectedRole == null) {
            errorLabel.setText("Sva polja su obavezna.");
            return;
        }
        if (password.length() < 8) {
            errorLabel.setText("Lozinka mora imati barem 8 znakova.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Lozinke se ne podudaraju.");
            return;
        }

        try {
            userRepository.registerUser(username, password, selectedRole); // Proslijeđivanje role
            showSuccessAlert();
            showLoginScreen();
        } catch (UsernameAlreadyExistsException e) {
            errorLabel.setText(e.getMessage());
            log.warn("Pokušaj registracije s postojećim korisničkim imenom: {}", username);
        } catch (Exception e) {
            errorLabel.setText("Dogodila se greška pri registraciji.");
            log.error("Greška pri registraciji.", e);
        }
    }

    /**
     * Vraća korisnika na ekran za prijavu.
     */
    @FXML
    private void showLoginScreen() {
        Navigation.showScene("login.fxml", "Supplier Payment System - Login");
    }

    private void showSuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registracija Uspješna");
        alert.setHeaderText(null);
        alert.setContentText("Korisnik '" + usernameField.getText() + "' je uspješno registriran. Možete se prijaviti.");
        alert.showAndWait();
    }
}