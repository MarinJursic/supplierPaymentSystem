package hr.javafx.projekt.controller;

import hr.javafx.projekt.enums.UserRole;
import hr.javafx.projekt.exception.RepositoryAccessException;
import hr.javafx.projekt.exception.UsernameAlreadyExistsException;
import hr.javafx.projekt.exception.ValidationException;
import hr.javafx.projekt.repository.UserRepository;
import hr.javafx.projekt.utils.DialogUtils;
import hr.javafx.projekt.utils.Navigation;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kontroler za ekran za registraciju novih korisnika.
 */
public class RegistrationController {

    private static final Logger log = LoggerFactory.getLogger(RegistrationController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<UserRole> roleComboBox;
    @FXML private Label errorLabel;

    private final UserRepository userRepository = new UserRepository();

    /**
     * Inicijalizira kontroler, popunjavajući ComboBox s dostupnim korisničkim rolama.
     */
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList(UserRole.values()));
        roleComboBox.getSelectionModel().select(UserRole.USER);
    }

    /**
     * Rukuje događajem registracije. Validira unos i registrira novog korisnika.
     */
    @FXML
    private void handleRegister() {
        try {
            validateInput();

            String username = usernameField.getText();
            String password = passwordField.getText();
            UserRole selectedRole = roleComboBox.getValue();

            userRepository.registerUser(username, password, selectedRole);
            DialogUtils.showInformation("Registracija Uspješna", "Korisnik '" + username + "' je uspješno registriran.");
            showLoginScreen();

        } catch (ValidationException e) {
            errorLabel.setText(e.getMessage());
            log.warn("Neuspješna validacija prilikom registracije.", e);
        } catch (UsernameAlreadyExistsException e) {
            errorLabel.setText(e.getMessage());
            log.warn("Pokušaj registracije s postojećim korisničkim imenom: {}", usernameField.getText(), e);
        } catch (RepositoryAccessException e) {
            errorLabel.setText("Greška pri pristupu podacima. Registracija nije uspjela.");
            log.error("Greška repozitorija prilikom registracije.", e);
        }
    }

    /**
     * Provjerava ispravnost unesenih podataka u formi za registraciju.
     * @throws ValidationException ako podaci nisu ispravni.
     */
    private void validateInput() throws ValidationException {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        UserRole selectedRole = roleComboBox.getValue();

        if (username.isBlank() || password.isEmpty() || confirmPassword.isEmpty() || selectedRole == null) {
            throw new ValidationException("Sva polja su obavezna.");
        }
        if (password.length() < 8) {
            throw new ValidationException("Lozinka mora imati barem 8 znakova.");
        }
        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Lozinke se ne podudaraju.");
        }

        errorLabel.setText("");
    }

    /**
     * Preusmjerava korisnika na ekran za prijavu.
     */
    @FXML
    private void showLoginScreen() {
        Navigation.showScene("login.fxml", "Supplier Payment System - Login");
    }
}