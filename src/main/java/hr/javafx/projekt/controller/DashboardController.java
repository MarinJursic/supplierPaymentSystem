package hr.javafx.projekt.controller;

import hr.javafx.projekt.session.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Kontroler za glavni (dashboard) ekran.
 */
public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private MenuController menuController;

    /**
     * Inicijalizira dashboard, postavlja poruke dobrodošlice.
     */
    public void initialize() {
        if (SessionManager.getLoggedInUserId() != null) {
            String role = SessionManager.isAdmin() ? "Administrator" : "Korisnik";
            welcomeLabel.setText("Dobrodošli u sustav!");
            roleLabel.setText("Prijavljeni ste kao: " + role);
        }

        if (menuController != null) {
            menuController.initialize();
        }
    }
}